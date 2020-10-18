package io.klinamen.joclray.kernels.post;

import io.klinamen.joclray.kernels.shading.ImageBuffer;

public class ImageMultiplyKernelParams {
    private final float weight;
    private final ImageBuffer imageBuffer;

    public ImageMultiplyKernelParams(float weight, ImageBuffer imageBuffer) {
        this.weight = weight;
        this.imageBuffer = imageBuffer;
    }

    public float getWeight() {
        return weight;
    }

    public ImageBuffer getImageBuffer() {
        return imageBuffer;
    }
}
