package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.shading.ImageBuffer;

public class PathTracingKernelParams {
    private final RaysBuffers raysBuffers;
    private final IntersectionKernelBuffers intersectionBuffers;
    private final ImageBuffer imageBuffer;
    private final ImageBuffer diffuseBuffer;
    private final DiffusePropsBuffers diffusePropsBuffers;

    public PathTracingKernelParams(RaysBuffers raysBuffers, IntersectionKernelBuffers intersectionBuffers, ImageBuffer imageBuffer, ImageBuffer diffuseBuffer, DiffusePropsBuffers diffusePropsBuffers) {
        this.raysBuffers = raysBuffers;
        this.intersectionBuffers = intersectionBuffers;
        this.imageBuffer = imageBuffer;
        this.diffuseBuffer = diffuseBuffer;
        this.diffusePropsBuffers = diffusePropsBuffers;
    }

    public RaysBuffers getRaysBuffers() {
        return raysBuffers;
    }

    public DiffusePropsBuffers getDiffusePropsBuffers() {
        return diffusePropsBuffers;
    }

    public IntersectionKernelBuffers getIntersectionBuffers() {
        return intersectionBuffers;
    }

    public ImageBuffer getImageBuffer() {
        return imageBuffer;
    }

    public ImageBuffer getDiffuseBuffer() {
        return diffuseBuffer;
    }
}
