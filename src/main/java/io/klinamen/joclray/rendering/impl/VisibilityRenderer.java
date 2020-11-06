package io.klinamen.joclray.rendering.impl;

import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.casting.RaysGenerationResult;
import io.klinamen.joclray.kernels.casting.ViewRaysKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysKernelParams;
import io.klinamen.joclray.kernels.intersection.IntersectResult;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.rendering.AbstractOpenCLRenderer;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;

public class VisibilityRenderer extends AbstractOpenCLRenderer {
    public VisibilityRenderer() {
    }

    public VisibilityRenderer(int platformIndex, int deviceIndex) {
        super(platformIndex, deviceIndex);
    }

    @Override
    protected float[] doRender(Scene scene) {
        final int nPixels = scene.getCamera().getPixels();

        RaysGenerationResult rays = new RaysGenerationResult(nPixels);
        RaysBuffers raysBuffers = RaysBuffers.create(getContext(), rays);
        ViewRaysKernelParams viewRaysKernelParams = new ViewRaysKernelParams(scene.getCamera().getImageWidth(), scene.getCamera().getImageHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), raysBuffers);
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

        float[] outBuffer = new float[nPixels * FloatVec4.DIM];

        int[] hitMap = intersectResult.getHitMap();

        int[] idToIndex = scene.getSurfaces().getIdToIndex();
        int nSurfaces = scene.getSurfaces().size();

        for (int i = 0; i < hitMap.length; i++) {
            int hitId = hitMap[i];

            float c = 0;
            if(hitId >= 0){
                c = 0.1f + (0.8f / nSurfaces) * idToIndex[hitId];
            }

            outBuffer[i * FloatVec4.DIM] = c;
            outBuffer[i * FloatVec4.DIM + 1] = c;
            outBuffer[i * FloatVec4.DIM + 2] = c;
        }

        return outBuffer;
    }
}
