package io.klinamen.joclray.rendering;

import io.klinamen.joclray.display.ShadingDisplay;
import io.klinamen.joclray.kernels.*;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.shading.*;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
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
    private final NewShadingOperation shadingOperation = new NewShadingOperation(getContext(), intersectionOp, splitRaysKernel, shadingKernel, 3);

    public TransmissionRenderer() {

    }

    public TransmissionRenderer(int platformIndex, int deviceIndex) {
        super(platformIndex, deviceIndex);
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();
        float[] imageBuffer = new float[nPixels * FloatVec4.DIM];

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

                try(ImageBuffer ib = ImageBuffer.create(getContext(), imageBuffer);
                    LightingBuffers lb = LightingBuffers.create(getContext(), scene, lightIntensityMap)
                ){
                    shadingOperation.setParams(new NewShadingOperationParams(viewRaysBuffers, viewRaysIntersectionsBuffers, lb, ib, scene));
                    shadingOperation.enqueue(getQueue());

                    ib.readTo(getQueue(), imageBuffer);
                }
            }
        }

        // update image
        new ShadingDisplay(scene, imageBuffer).update(outImage);
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
