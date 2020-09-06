package io.klinamen.joclray.kernels;

import io.klinamen.joclray.FloatVec4;
import io.klinamen.joclray.scene.Scene;

public class ViewRaysKernelParams {
    private final int imageWidth;
    private final int imageHeight;
    private final FloatVec4 viewOrigin;
    private final float fovRad;
    private final RaysBuffers buffers;

    public ViewRaysKernelParams(int imageWidth, int imageHeight, FloatVec4 viewOrigin, float fovRad, RaysBuffers buffers) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.viewOrigin = viewOrigin;
        this.fovRad = fovRad;
        this.buffers = buffers;
    }

    public static ViewRaysKernelParams create(Scene scene, RaysBuffers buffers) {
        int imageWidth = (int) scene.getCamera().getFrameWidth();
        int imageHeight = (int) scene.getCamera().getFrameHeight();

        return new ViewRaysKernelParams(imageWidth, imageHeight, scene.getOrigin(), scene.getCamera().getFovRad(), buffers);
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public FloatVec4 getViewOrigin() {
        return viewOrigin;
    }

    public float getFovRad() {
        return fovRad;
    }

    public RaysBuffers getBuffers() {
        return buffers;
    }
}
