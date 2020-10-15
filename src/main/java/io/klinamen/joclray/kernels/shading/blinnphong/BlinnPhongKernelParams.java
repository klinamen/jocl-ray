package io.klinamen.joclray.kernels.shading.blinnphong;

import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.kernels.shading.LightingBuffers;
import io.klinamen.joclray.kernels.shading.WeightedRaysBuffer;

public class BlinnPhongKernelParams {
    private final float ambientLightIntensity;
    private final int totalLights;
    private final BlinnPhongMaterialPropsBuffers materialPropsBuffers;
    private final LightingBuffers lightingBuffers;
    private final WeightedRaysBuffer viewRaysBuffer;
    private final IntersectionKernelBuffers viewRaysIntersectionBuffers;
    private final ImageBuffer imageBuffer;

    public BlinnPhongKernelParams(WeightedRaysBuffer viewRaysBuffer, IntersectionKernelBuffers viewRaysIntersectionBuffers, BlinnPhongMaterialPropsBuffers materialPropsBuffers, LightingBuffers lightingBuffers, ImageBuffer imageBuffer, float ambientLightIntensity, int totalLights) {
        this.viewRaysBuffer = viewRaysBuffer;
        this.materialPropsBuffers = materialPropsBuffers;
        this.lightingBuffers = lightingBuffers;
        this.imageBuffer = imageBuffer;
        this.ambientLightIntensity = ambientLightIntensity;
        this.totalLights = totalLights;
        this.viewRaysIntersectionBuffers = viewRaysIntersectionBuffers;
    }

    public float getAmbientLightIntensity() {
        return ambientLightIntensity;
    }

    public int getTotalLights() {
        return totalLights;
    }

    public BlinnPhongMaterialPropsBuffers getMaterialPropsBuffers() {
        return materialPropsBuffers;
    }

    public LightingBuffers getLightingBuffers() {
        return lightingBuffers;
    }

    public WeightedRaysBuffer getViewRaysBuffer() {
        return viewRaysBuffer;
    }

    public IntersectionKernelBuffers getViewRaysIntersectionBuffers() {
        return viewRaysIntersectionBuffers;
    }

    public ImageBuffer getImageBuffer() {
        return imageBuffer;
    }
}
