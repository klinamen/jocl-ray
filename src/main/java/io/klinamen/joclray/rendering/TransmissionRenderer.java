package io.klinamen.joclray.rendering;

import io.klinamen.joclray.display.ShadingDisplay;
import io.klinamen.joclray.kernels.LightIntensityMapOperation;
import io.klinamen.joclray.kernels.LightIntensityMapOperationParams;
import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.casting.ShadowRaysKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysKernelParams;
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

public class TransmissionRenderer extends OpenCLRenderer implements AutoCloseable {
    private final IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());

    private final ViewRaysKernel viewRaysKernel = new ViewRaysKernel(getContext());
    private final ShadowRaysKernel shadowRaysKernel = new ShadowRaysKernel(getContext());
    private final BlinnPhongKernel shadingKernel = new BlinnPhongKernel(getContext());
    private final SplitRaysKernel splitRaysKernel = new SplitRaysKernel(getContext());

    private final IntersectionOperation intersectionOp = new IntersectionOperation(intersectionKernelFactory);
    private final LightIntensityMapOperation lightIntensityMapOperation = new LightIntensityMapOperation(getContext(), shadowRaysKernel, intersectionOp);
    private final TransmissionTracingOperation shadingOperation = new TransmissionTracingOperation(getContext(), intersectionOp, splitRaysKernel, shadingKernel, 3);

    public TransmissionRenderer() {

    }

    public TransmissionRenderer(int platformIndex, int deviceIndex) {
        super(platformIndex, deviceIndex);
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();
        float[] outImageBuf = new float[nPixels * FloatVec4.DIM];

        try(RaysBuffers viewRaysBuffers = RaysBuffers.empty(getContext(), nPixels)){
            // generate view rays
            viewRaysKernel.setParams(new ViewRaysKernelParams(outImage.getWidth(), outImage.getHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers));
            viewRaysKernel.enqueue(getQueue());

            try (IntersectionKernelBuffers viewRaysIntersectionsBuffers = IntersectionKernelBuffers.empty(getContext(), nPixels)) {
                // primary ray intersections
                intersectionOp.setParams(new IntersectionOperationParams(scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers));
                intersectionOp.enqueue(getQueue());

                // compute light intensity map (for shadows)
                LightIntensityMapOperationParams lightIntensityMapOperationParams = new LightIntensityMapOperationParams(scene.getLightElements(), scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers);
                lightIntensityMapOperation.setParams(lightIntensityMapOperationParams);
                lightIntensityMapOperation.enqueue(getQueue());

                float[] lightIntensityMap = lightIntensityMapOperationParams.getLightIntensityMap();

                try(ImageBuffer ib = ImageBuffer.create(getContext(), outImageBuf);
                    LightingBuffers lb = LightingBuffers.create(getContext(), scene, lightIntensityMap)
                ){
                    shadingOperation.setParams(new TransmissionTracingOperationParams(viewRaysBuffers, viewRaysIntersectionsBuffers, lb, ib, scene));
                    shadingOperation.enqueue(getQueue());

                    ib.readTo(getQueue(), outImageBuf);
                }
            }
        }

        // update image
        new ShadingDisplay(scene, outImageBuf).update(outImage);
    }

    @Override
    public void close() {
        super.close();

        viewRaysKernel.close();
        shadowRaysKernel.close();
        splitRaysKernel.close();
        shadingKernel.close();

        if(intersectionKernelFactory instanceof AutoCloseable){
            try {
                ((AutoCloseable) intersectionKernelFactory).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
