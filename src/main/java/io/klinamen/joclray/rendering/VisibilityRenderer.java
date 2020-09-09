package io.klinamen.joclray.rendering;

import io.klinamen.joclray.display.IntersectionsDisplay;
import io.klinamen.joclray.kernels.RaysBuffers;
import io.klinamen.joclray.kernels.RaysGenerationResult;
import io.klinamen.joclray.kernels.ViewRaysKernel;
import io.klinamen.joclray.kernels.ViewRaysKernelParams;
import io.klinamen.joclray.kernels.intersection.IntersectResult;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.scene.Scene;

import java.awt.image.BufferedImage;

public class VisibilityRenderer extends OpenCLRenderer {
    public VisibilityRenderer() {
    }

    public VisibilityRenderer(int platformIndex, int deviceIndex) {
        super(platformIndex, deviceIndex);
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();

        RaysGenerationResult rays = new RaysGenerationResult(nPixels);
        RaysBuffers raysBuffers = RaysBuffers.create(getContext(), rays);
        ViewRaysKernelParams viewRaysKernelParams = new ViewRaysKernelParams(outImage.getWidth(), outImage.getHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), raysBuffers);
        ViewRaysKernel viewRaysKernel = new ViewRaysKernel(getContext());
        viewRaysKernel.setParams(viewRaysKernelParams);

        IntersectResult intersectResult = new IntersectResult(nPixels);
        RegistryIntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());
        IntersectionKernelBuffers intersectionKernelBuffers = IntersectionKernelBuffers.fromResult(getContext(), intersectResult);
        IntersectionOperationParams intersectionOperationParams = new IntersectionOperationParams(scene.getSurfaces(), raysBuffers, intersectionKernelBuffers);
        IntersectionOperation intersectionOperation = new IntersectionOperation(intersectionKernelFactory);
        intersectionOperation.setParams(intersectionOperationParams);

        // cast
        viewRaysKernel.enqueue(getQueue());
        intersectionOperation.enqueue(getQueue());

        intersectionKernelBuffers.readTo(getQueue(), intersectResult);

        // update image
        new IntersectionsDisplay(scene, intersectResult).update(outImage);
    }
}
