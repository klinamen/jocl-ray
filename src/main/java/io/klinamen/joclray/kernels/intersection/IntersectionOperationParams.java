package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.RaysBuffers;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;

public class IntersectionOperationParams {
    private final ElementSet<SurfaceElement<Surface>> surfaces;
    private final IntersectionKernelBuffers intersectionKernelBuffers;
    private final RaysBuffers raysBuffers;

    public IntersectionOperationParams(ElementSet<SurfaceElement<Surface>> surfaces, RaysBuffers raysBuffers, IntersectionKernelBuffers intersectionKernelBuffers) {
        this.raysBuffers = raysBuffers;
        this.intersectionKernelBuffers = intersectionKernelBuffers;
        this.surfaces = surfaces;
    }

    public ElementSet<SurfaceElement<Surface>> getSurfaces() {
        return surfaces;
    }

    public IntersectionKernelBuffers getIntersectionKernelBuffers() {
        return intersectionKernelBuffers;
    }

    public RaysBuffers getRaysBuffers() {
        return raysBuffers;
    }
}
