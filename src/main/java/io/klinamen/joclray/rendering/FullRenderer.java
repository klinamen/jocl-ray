package io.klinamen.joclray.rendering;

import io.klinamen.joclray.display.ShadingDisplay;
import io.klinamen.joclray.kernels.*;
import io.klinamen.joclray.kernels.intersection.IntersectResult;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.shading.ShadingKernel;
import io.klinamen.joclray.kernels.shading.ShadingKernelBuffers;
import io.klinamen.joclray.kernels.shading.ShadingKernelParams;
import io.klinamen.joclray.kernels.shading.ShadingOperation;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;

import java.awt.image.BufferedImage;

public class FullRenderer extends OpenCLRenderer implements AutoCloseable {
    private final IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());

    private final ViewRaysKernel viewRaysKernel = new ViewRaysKernel(getContext());
    private final ShadowRaysKernel shadowRaysKernel = new ShadowRaysKernel(getContext());
    private final ShadingKernel shadingKernel = new ShadingKernel(getContext());

    private final IntersectionOperation intersectionOp = new IntersectionOperation(intersectionKernelFactory);
    private final LightIntensityMapOperation lightIntensityMapOperation = new LightIntensityMapOperation(getContext(), shadowRaysKernel, intersectionOp);
    private final ShadingOperation shadingOperation = new ShadingOperation(intersectionOp, shadingKernel, 4);

    public FullRenderer() {

    }

    public FullRenderer(int platformIndex, int deviceIndex) {
        super(platformIndex, deviceIndex);
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();
        float[] imageBuffer = new float[nPixels * FloatVec4.DIM];

        RaysGenerationResult rays = new RaysGenerationResult(nPixels);

        try(RaysBuffers viewRaysBuffers = RaysBuffers.create(getContext(), rays)){
            // generate view rays
            viewRaysKernel.setParams(new ViewRaysKernelParams(outImage.getWidth(), outImage.getHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers));
            viewRaysKernel.enqueue(getQueue());

            IntersectResult intersectResult = new IntersectResult(nPixels);

            try (IntersectionKernelBuffers viewRaysIntersectionsBuffers = IntersectionKernelBuffers.fromResult(getContext(), intersectResult)) {
                // primary ray intersections
                intersectionOp.setParams(new IntersectionOperationParams(scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers));
                intersectionOp.enqueue(getQueue());

                // compute light intensity map (for shadows)
                LightIntensityMapOperationParams lightIntensityMapOperationParams = new LightIntensityMapOperationParams(scene.getLightElements(), scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers);
                lightIntensityMapOperation.setParams(lightIntensityMapOperationParams);
                lightIntensityMapOperation.enqueue(getQueue());

                float[] lightIntensityMap = lightIntensityMapOperationParams.getLightIntensityMap();

                try (ShadingKernelBuffers shadingKernelBuffers = ShadingKernelBuffers.create(getContext(), nPixels, scene, lightIntensityMap, imageBuffer)) {
                    // shading
                    intersectionOp.setParams(new IntersectionOperationParams(scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers));
                    shadingKernel.setParams(new ShadingKernelParams(
                            viewRaysBuffers, viewRaysIntersectionsBuffers, shadingKernelBuffers,
                            scene.getAmbientLightIntensity(), scene.getLightElements().size()
                    ));

                    shadingOperation.enqueue(getQueue());

                    // read image output buffer
                    shadingKernelBuffers.readTo(getQueue(), imageBuffer);
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
