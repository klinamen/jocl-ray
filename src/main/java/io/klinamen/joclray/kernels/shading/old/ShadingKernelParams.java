package io.klinamen.joclray.kernels.shading.old;

import io.klinamen.joclray.kernels.RaysBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.scene.Scene;

public class ShadingKernelParams {
    private final float ambientLightIntensity;
    private final int totalLights;
    private final ShadingKernelBuffers buffers;
    private final RaysBuffers viewRaysBuffer;
    private final IntersectionKernelBuffers viewRaysIntersectionBuffers;

    public ShadingKernelParams(RaysBuffers viewRaysBuffer, IntersectionKernelBuffers viewRaysIntersectionBuffers, ShadingKernelBuffers buffers, float ambientLightIntensity, int totalLights) {
        this.viewRaysBuffer = viewRaysBuffer;
        this.ambientLightIntensity = ambientLightIntensity;
        this.totalLights = totalLights;
        this.buffers = buffers;
        this.viewRaysIntersectionBuffers = viewRaysIntersectionBuffers;
    }

    public float getAmbientLightIntensity() {
        return ambientLightIntensity;
    }

    public int getTotalLights() {
        return totalLights;
    }

    public ShadingKernelBuffers getBuffers() {
        return buffers;
    }

    public RaysBuffers getViewRaysBuffer() {
        return viewRaysBuffer;
    }

    public IntersectionKernelBuffers getViewRaysIntersectionBuffers() {
        return viewRaysIntersectionBuffers;
    }

    public static ShadingKernelParams fromScene(Scene scene, RaysBuffers viewRaysBuffer, IntersectionKernelBuffers viewRaysIntersectionBuffers, ShadingKernelBuffers buffers) {
        return new ShadingKernelParams(viewRaysBuffer, viewRaysIntersectionBuffers, buffers, scene.getAmbientLightIntensity(), scene.getLightElements().size());
    }
}
