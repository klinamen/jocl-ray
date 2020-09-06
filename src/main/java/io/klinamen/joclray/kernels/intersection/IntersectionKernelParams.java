package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.RaysBuffers;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;

public class IntersectionKernelParams<T extends Surface> {
    private final ElementSet<SurfaceElement<T>> surfaces;
    private final IntersectionKernelBuffers intersectionBuffers;
    private final RaysBuffers raysBuffers;

    public IntersectionKernelParams(ElementSet<SurfaceElement<T>> surfaces, IntersectionKernelBuffers intersectionBuffers, RaysBuffers raysBuffers) {
        this.surfaces = surfaces;
        this.intersectionBuffers = intersectionBuffers;
        this.raysBuffers = raysBuffers;
    }

    public ElementSet<SurfaceElement<T>> getSurfaces() {
        return surfaces;
    }

    public IntersectionKernelBuffers getIntersectionBuffers() {
        return intersectionBuffers;
    }

    public RaysBuffers getRaysBuffers() {
        return raysBuffers;
    }
}
