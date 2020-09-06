package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.scene.Scene;

public class ShadingKernelParams {
    private final float ambientLightIntensity;
    private final int totalLights;
    private final ShadingKernelBuffers buffers;

    public ShadingKernelParams(float ambientLightIntensity, int totalLights, ShadingKernelBuffers buffers) {
        this.ambientLightIntensity = ambientLightIntensity;
        this.totalLights = totalLights;
        this.buffers = buffers;
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

    public static ShadingKernelParams fromScene(Scene scene, ShadingKernelBuffers buffers) {
        return new ShadingKernelParams(scene.getAmbientLightIntensity(), scene.getLightElements().size(), buffers);
    }
}
