package io.klinamen.joclray.kernels.casting;

import io.klinamen.joclray.util.FloatVec4;

public class ViewRaysJitterKernelParams extends ViewRaysKernelParams {
    private final int hSamples;
    private final int vSamples;
    private final int xIndex;
    private final int yIndex;
    private final long seed;

    public ViewRaysJitterKernelParams(int imageWidth, int imageHeight, FloatVec4 viewOrigin, float fovRad, RaysBuffers buffers, int hSamples, int vSamples, int xIndex, int yIndex, Long seed) {
        super(imageWidth, imageHeight, viewOrigin, fovRad, buffers);
        this.hSamples = hSamples;
        this.vSamples = vSamples;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.seed = seed;
    }

    public ViewRaysJitterKernelParams(int imageWidth, int imageHeight, FloatVec4 viewOrigin, float fovRad, RaysBuffers buffers, int hSamples, int vSamples, int xIndex, int yIndex) {
        super(imageWidth, imageHeight, viewOrigin, fovRad, buffers);
        this.hSamples = hSamples;
        this.vSamples = vSamples;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.seed = (long) (Long.MAX_VALUE * Math.random());
    }

    public int gethSamples() {
        return hSamples;
    }

    public int getvSamples() {
        return vSamples;
    }

    public int getxIndex() {
        return xIndex;
    }

    public int getyIndex() {
        return yIndex;
    }

    public long getSeed() {
        return seed;
    }
}
