package io.klinamen.joclray.rendering;

import io.klinamen.joclray.display.ShadingDisplay;
import io.klinamen.joclray.kernels.ImageMultiplyKernel;
import io.klinamen.joclray.kernels.ImageMultiplyKernelParams;
import io.klinamen.joclray.kernels.LightIntensityMapOperation;
import io.klinamen.joclray.kernels.LightIntensityMapOperationParams;
import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.casting.ShadowRaysKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysJitterKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysJitterKernelParams;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.kernels.shading.SplitRaysKernel;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
import io.klinamen.joclray.kernels.tracing.LightingBuffers;
import io.klinamen.joclray.kernels.tracing.TransmissionTracingOperation;
import io.klinamen.joclray.kernels.tracing.TransmissionTracingOperationParams;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;

import java.awt.image.BufferedImage;

public class MultiPassTransmissionRenderer extends OpenCLRenderer implements AutoCloseable {
    private final IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());

    private final ViewRaysJitterKernel viewRaysKernel = new ViewRaysJitterKernel(getContext());
    private final ShadowRaysKernel shadowRaysKernel = new ShadowRaysKernel(getContext());
    private final BlinnPhongKernel shadingKernel = new BlinnPhongKernel(getContext());
    private final SplitRaysKernel splitRaysKernel = new SplitRaysKernel(getContext());
    private final ImageMultiplyKernel imageMultiplyKernel = new ImageMultiplyKernel(getContext());

    private final IntersectionOperation intersectionOp = new IntersectionOperation(intersectionKernelFactory);
    private final LightIntensityMapOperation lightIntensityMapOperation = new LightIntensityMapOperation(getContext(), shadowRaysKernel, intersectionOp);
    private final TransmissionTracingOperation shadingOperation = new TransmissionTracingOperation(getContext(), intersectionOp, splitRaysKernel, shadingKernel, 3);

    private final int samples;

    public MultiPassTransmissionRenderer(int platformIndex, int deviceIndex, int samples) {
        super(platformIndex, deviceIndex);
        this.samples = samples;
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();
        float[] outImageBuf = new float[nPixels * FloatVec4.DIM];

        try (ImageBuffer imageBuffer = ImageBuffer.create(getContext(), outImageBuf)) {
            for (int i = 0; i < samples; i++) {
                for (int j = 0; j < samples; j++) {
                    System.out.printf("Sample (%d/%d, %d/%d)" + System.lineSeparator(), i + 1, samples, j + 1, samples);

                    try (RaysBuffers viewRaysBuffers = RaysBuffers.empty(getContext(), scene.getCamera().getPixels())) {
                        // generate view rays
                        viewRaysKernel.setParams(new ViewRaysJitterKernelParams(
                                scene.getCamera().getImageWidth(), scene.getCamera().getImageHeight(),
                                scene.getOrigin(), scene.getCamera().getFovRad(),
                                viewRaysBuffers, samples, samples, i, j
                        ));
                        viewRaysKernel.enqueue(getQueue());

                        pass(scene, imageBuffer, viewRaysBuffers);
                    }
                }
            }

            // Average pixel values
            imageMultiplyKernel.setParams(new ImageMultiplyKernelParams(1.0f / (float)(samples * samples), imageBuffer));
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
                shadingOperation.setParams(new TransmissionTracingOperationParams(viewRaysBuffers, viewRaysIntersectionsBuffers, lb, imageBuffer, scene));
                shadingOperation.enqueue(getQueue());
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
