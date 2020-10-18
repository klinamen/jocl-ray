package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.scene.Scene;

public class TracingOperationParams {
    private final RaysBuffers viewRaysBuffer;
    private final IntersectionKernelBuffers intersectionBuffers;
    private final LightingBuffers lightingBuffers;
    private final ImageBuffer imageBuffer;

    private final Scene scene;

    public TracingOperationParams(RaysBuffers viewRaysBuffer, IntersectionKernelBuffers intersectionBuffers, LightingBuffers lightingBuffers, ImageBuffer imageBuffer, Scene scene) {
        this.viewRaysBuffer = viewRaysBuffer;
        this.intersectionBuffers = intersectionBuffers;
        this.lightingBuffers = lightingBuffers;
        this.imageBuffer = imageBuffer;
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public RaysBuffers getViewRaysBuffer() {
        return viewRaysBuffer;
    }

    public IntersectionKernelBuffers getIntersectionBuffers() {
        return intersectionBuffers;
    }

    public ImageBuffer getImageBuffer() {
        return imageBuffer;
    }

    public LightingBuffers getLightingBuffers() {
        return lightingBuffers;
    }
}
