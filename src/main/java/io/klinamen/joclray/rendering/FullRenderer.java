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
    public FullRenderer() {
    }

    public FullRenderer(int platformIndex, int deviceIndex) {
        super(platformIndex, deviceIndex);
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();
        float[] imageBuffer = new float[nPixels * FloatVec4.DIM];

//        IntersectionKernelFactory intersectionKernelFactory = new PrototypeIntersectionKernelFactory(getContext());
        IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());

        RaysGenerationResult rays = new RaysGenerationResult(nPixels);
        RaysBuffers viewRaysBuffers = RaysBuffers.create(getContext(), rays);
        ViewRaysKernelParams viewRaysKernelParams = new ViewRaysKernelParams(outImage.getWidth(), outImage.getHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers);
        ViewRaysKernel viewRaysKernel = new ViewRaysKernel(getContext());
        viewRaysKernel.setParams(viewRaysKernelParams);

        // generate view rays
        viewRaysKernel.enqueue(getQueue());

        IntersectResult intersectResult = new IntersectResult(nPixels);
        IntersectionKernelBuffers viewRaysIntersectionsBuffers = IntersectionKernelBuffers.fromResult(getContext(), intersectResult);
        IntersectionOperationParams viewRayIntersectionParams = new IntersectionOperationParams(scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers);
        IntersectionOperation viewRaysIntersection = new IntersectionOperation(intersectionKernelFactory);
        viewRaysIntersection.setParams(viewRayIntersectionParams);

        // primary ray intersections
        viewRaysIntersection.enqueue(getQueue());

        LightIntensityMapOperationParams lightIntensityMapOperationParams = new LightIntensityMapOperationParams(scene.getLightElements(), scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers);
        LightIntensityMapOperation lightIntensityMapOperation = new LightIntensityMapOperation(getContext(), intersectionKernelFactory);
        lightIntensityMapOperation.setParams(lightIntensityMapOperationParams);

        // compute light intensity map (for shadows)
        lightIntensityMapOperation.enqueue(getQueue());

        float[] lightIntensityMap = lightIntensityMapOperationParams.getLightIntensityMap();

        ShadingKernelBuffers shadingKernelBuffers = ShadingKernelBuffers.create(getContext(), viewRaysBuffers, viewRaysIntersectionsBuffers, scene, lightIntensityMap, imageBuffer);
        ShadingKernelParams shadingKernelParams = new ShadingKernelParams(scene.getAmbientLightIntensity(), scene.getLightElements().size(), shadingKernelBuffers);
        ShadingKernel shadingKernel = new ShadingKernel(getContext());
        shadingKernel.setParams(shadingKernelParams);

        ShadingOperation shadingOperation = new ShadingOperation(viewRaysIntersection, shadingKernel, 4);

        // shading
        shadingOperation.enqueue(getQueue());

        shadingKernelBuffers.readTo(getQueue(), imageBuffer);

        // update image
        new ShadingDisplay(scene, imageBuffer).update(outImage);
    }
}
