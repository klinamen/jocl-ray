package io.klinamen.joclray.rendering;

import io.klinamen.joclray.display.ShadingDisplay;
import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.casting.ShadowRaysKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysJitterKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysJitterKernelParams;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernel;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernelParams;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.kernels.shading.LightIntensityMapOperation;
import io.klinamen.joclray.kernels.shading.LightIntensityMapOperationParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
import io.klinamen.joclray.kernels.tracing.LightingBuffers;
import io.klinamen.joclray.kernels.tracing.SplitRaysDistKernel;
import io.klinamen.joclray.kernels.tracing.TracingOperation;
import io.klinamen.joclray.kernels.tracing.TracingOperationParams;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;

import java.awt.image.BufferedImage;

public class DistributionRayTracerRenderer extends OpenCLRenderer implements AutoCloseable {
    private final IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());

    private final ViewRaysJitterKernel viewRaysKernel = new ViewRaysJitterKernel(getContext());
    private final ShadowRaysKernel shadowRaysKernel = new ShadowRaysKernel(getContext());
    private final BlinnPhongKernel shadingKernel = new BlinnPhongKernel(getContext());
    private final ImageMultiplyKernel imageMultiplyKernel = new ImageMultiplyKernel(getContext());

    private final IntersectionOperation intersectionOp = new IntersectionOperation(intersectionKernelFactory);
    private final LightIntensityMapOperation lightIntensityMapOperation = new LightIntensityMapOperation(getContext(), shadowRaysKernel, intersectionOp);

//    private final SplitRaysKernel splitRaysKernel = new SplitRaysKernel(getContext());
    private final SplitRaysDistKernel splitRaysKernel = new SplitRaysDistKernel(getContext(), 16, 0.04f);
    private final TracingOperation tracingOperation = new TracingOperation(getContext(), intersectionOp, splitRaysKernel, shadingKernel, 3);

    private final int ipsSamples;
    private final int essSamples;

    public DistributionRayTracerRenderer(int platformIndex, int deviceIndex, int ipsSamples, int essSamples) {
        super(platformIndex, deviceIndex);
        this.ipsSamples = ipsSamples;
        this.essSamples = essSamples;
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();
        float[] outImageBuf = new float[nPixels * FloatVec4.DIM];

        try (ImageBuffer imageBuffer = ImageBuffer.create(getContext(), outImageBuf)) {
            for (int k = 0; k < essSamples; k++) {
                System.out.printf("Eye-space sample (%d/%d)" + System.lineSeparator(), k + 1, essSamples);

                for (int i = 0; i < ipsSamples; i++) {
                    for (int j = 0; j < ipsSamples; j++) {
                        System.out.printf("Image plane sample (%d/%d, %d/%d)" + System.lineSeparator(), i + 1, ipsSamples, j + 1, ipsSamples);

                        try (RaysBuffers viewRaysBuffers = RaysBuffers.empty(getContext(), scene.getCamera().getPixels())) {
                            // generate view rays
                            viewRaysKernel.setParams(new ViewRaysJitterKernelParams(
                                    scene.getCamera().getImageWidth(), scene.getCamera().getImageHeight(), scene.getOrigin(),
                                    scene.getCamera().getFovRad(), scene.getCamera().getAperture(), scene.getCamera().getFocalLength(),
                                    viewRaysBuffers, ipsSamples, ipsSamples, i, j
                            ));
                            viewRaysKernel.enqueue(getQueue());

                            pass(scene, imageBuffer, viewRaysBuffers);
                        }
                    }
                }
            }

            // Average pixel values
            imageMultiplyKernel.setParams(new ImageMultiplyKernelParams(1.0f / (float) (essSamples * ipsSamples * ipsSamples * splitRaysKernel.getSamples()), imageBuffer));
            imageMultiplyKernel.enqueue(getQueue());

            imageBuffer.readTo(getQueue(), outImageBuf);
        }

        // update image
        new ShadingDisplay(scene, outImageBuf).update(outImage);
    }

    private void pass(Scene scene, ImageBuffer imageBuffer, RaysBuffers viewRaysBuffers) {
        try (IntersectionKernelBuffers viewRaysIntersectionsBuffers = IntersectionKernelBuffers.empty(getContext(), scene.getCamera().getPixels())) {
            // primary ray intersections
            intersectionOp.setParams(new IntersectionOperationParams(scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers));
            intersectionOp.enqueue(getQueue());

            // compute light intensity map (for shadows)
            LightIntensityMapOperationParams lightIntensityMapOperationParams = new LightIntensityMapOperationParams(scene.getLightElements(), scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers);
            lightIntensityMapOperation.setParams(lightIntensityMapOperationParams);
            lightIntensityMapOperation.enqueue(getQueue());

            float[] lightIntensityMap = lightIntensityMapOperationParams.getLightIntensityMap();

            try (LightingBuffers lb = LightingBuffers.create(getContext(), scene, lightIntensityMap)) {
                for (int i = 0; i < splitRaysKernel.getSamples(); i++) {
                    System.out.println(String.format("Reflection/Transmission sample %d/%d", i + 1, splitRaysKernel.getSamples()));

                    splitRaysKernel.seed();
                    tracingOperation.setParams(new TracingOperationParams(viewRaysBuffers, viewRaysIntersectionsBuffers, lb, imageBuffer, scene));
                    tracingOperation.enqueue(getQueue());
                }
            }
        }
    }

    @Override
    public void close() {
        super.close();

        viewRaysKernel.close();
        shadowRaysKernel.close();
        splitRaysKernel.close();
        shadingKernel.close();
        imageMultiplyKernel.close();

        if (intersectionKernelFactory instanceof AutoCloseable) {
            try {
                ((AutoCloseable) intersectionKernelFactory).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
