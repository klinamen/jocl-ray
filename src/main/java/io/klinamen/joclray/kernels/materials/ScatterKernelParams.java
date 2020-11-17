package io.klinamen.joclray.kernels.materials;

import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.scene.Scene;

public class ScatterKernelParams {
    private final RayQueueBuffers rayQueueBuffers;
    private final RaysBuffers raysBuffers;
    private final IntersectionKernelBuffers intersectionBuffers;
    private final ImageBuffer throughputBuffer;
    private final ImageBuffer radianceBuffer;
    private final Scene scene;

    private int queueOffset;
    private int queueSize;

    public ScatterKernelParams(Scene scene, RayQueueBuffers rayQueueBuffers, RaysBuffers raysBuffers, IntersectionKernelBuffers intersectionBuffers, ImageBuffer radianceBuffer, ImageBuffer throughputBuffer, int queueOffset, int queueSize) {
        this.rayQueueBuffers = rayQueueBuffers;
        this.raysBuffers = raysBuffers;
        this.intersectionBuffers = intersectionBuffers;
        this.radianceBuffer = radianceBuffer;
        this.queueOffset = queueOffset;
        this.queueSize = queueSize;
        this.scene = scene;
        this.throughputBuffer = throughputBuffer;
    }

    public int getQueueOffset() {
        return queueOffset;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public ScatterKernelParams setQueueOffset(int queueOffset) {
        this.queueOffset = queueOffset;
        return this;
    }

    public ScatterKernelParams setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public RayQueueBuffers getRayQueueBuffers() {
        return rayQueueBuffers;
    }

    public ImageBuffer getRadianceBuffer() {
        return radianceBuffer;
    }

    public RaysBuffers getRaysBuffers() {
        return raysBuffers;
    }

    public ImageBuffer getThroughputBuffer() {
        return throughputBuffer;
    }

    public IntersectionKernelBuffers getIntersectionBuffers() {
        return intersectionBuffers;
    }

    public Scene getScene() {
        return scene;
    }
}
