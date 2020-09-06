package io.klinamen.joclray.kernels;

import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.LightElement;

public class ShadowRaysKernelParams {
    private final ElementSet<LightElement> lights;
    private final RaysBuffers viewRaysBuffers;
    private final IntersectionKernelBuffers intersectionKernelBuffers;
    private final RaysBuffers shadowRaysBuffers;

    public ShadowRaysKernelParams(ElementSet<LightElement> lights, RaysBuffers viewRaysBuffer, IntersectionKernelBuffers intersectionKernelBuffers, RaysBuffers shadowRaysBuffers) {
        this.lights = lights;
        this.viewRaysBuffers = viewRaysBuffer;
        this.intersectionKernelBuffers = intersectionKernelBuffers;
        this.shadowRaysBuffers = shadowRaysBuffers;
    }

    public static ShadowRaysKernelParams create(ElementSet<LightElement> lights, RaysBuffers viewRaysBuffer, IntersectionKernelBuffers intersectionKernelBuffers, RaysBuffers shadowRaysBuffers) {
        return new ShadowRaysKernelParams(lights, viewRaysBuffer, intersectionKernelBuffers, shadowRaysBuffers);
    }

    public RaysBuffers getViewRaysBuffers() {
        return viewRaysBuffers;
    }

    public RaysBuffers getShadowRaysBuffers() {
        return shadowRaysBuffers;
    }

    public IntersectionKernelBuffers getIntersectionKernelBuffers() {
        return intersectionKernelBuffers;
    }

    public ElementSet<LightElement> getLights() {
        return lights;
    }
}
