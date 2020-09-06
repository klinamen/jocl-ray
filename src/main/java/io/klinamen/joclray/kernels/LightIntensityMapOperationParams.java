package io.klinamen.joclray.kernels;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.LightElement;
import io.klinamen.joclray.scene.SurfaceElement;

public class LightIntensityMapOperationParams {
    private final ElementSet<LightElement> lights;
    private final ElementSet<SurfaceElement<Surface>> surfaces;
    private final RaysBuffers viewRaysBuffer;
    private final IntersectionKernelBuffers viewRaysIntersectionsBuffers;

    private float[] lightIntensityMap;

    public LightIntensityMapOperationParams(ElementSet<LightElement> lights, ElementSet<SurfaceElement<Surface>> surfaces, RaysBuffers viewRaysBuffer, IntersectionKernelBuffers viewRaysIntersectionsBuffers) {
        this.surfaces = surfaces;
        this.viewRaysBuffer = viewRaysBuffer;
        this.viewRaysIntersectionsBuffers = viewRaysIntersectionsBuffers;
        this.lights = lights;
    }

    public RaysBuffers getViewRaysBuffer() {
        return viewRaysBuffer;
    }

    public IntersectionKernelBuffers getViewRaysIntersectionsBuffers() {
        return viewRaysIntersectionsBuffers;
    }

    public ElementSet<LightElement> getLights() {
        return lights;
    }

    public ElementSet<SurfaceElement<Surface>> getSurfaces() {
        return surfaces;
    }

    public float[] getLightIntensityMap() {
        return lightIntensityMap;
    }

    public LightIntensityMapOperationParams setLightIntensityMap(float[] lightIntensityMap) {
        this.lightIntensityMap = lightIntensityMap;
        return this;
    }
}
