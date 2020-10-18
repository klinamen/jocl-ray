package io.klinamen.joclray.kernels.casting;

import io.klinamen.joclray.util.FloatVec4;

public class ViewRaysJitterKernelParams extends ViewRaysKernelParams {
    private final float aperture;
    private final float focalLength;
    private final int hSamples;
    private final int vSamples;
    private final int xIndex;
    private final int yIndex;
    private final long ipsSeed;
    private final long essSeed;

    public ViewRaysJitterKernelParams(int imageWidth, int imageHeight, FloatVec4 viewOrigin, float fovRad, RaysBuffers buffers, float aperture, float focalLength, int hSamples, int vSamples, int xIndex, int yIndex, long ipsSeed, long essSeed) {
        super(imageWidth, imageHeight, viewOrigin, fovRad, buffers);
        this.aperture = aperture;
        this.focalLength = focalLength;
        this.hSamples = hSamples;
        this.vSamples = vSamples;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.ipsSeed = ipsSeed;
        this.essSeed = essSeed;
    }

    public ViewRaysJitterKernelParams(int imageWidth, int imageHeight, FloatVec4 viewOrigin, float fovRad, float aperture, float focalLength, RaysBuffers buffers, int hSamples, int vSamples, int xIndex, int yIndex) {
        super(imageWidth, imageHeight, viewOrigin, fovRad, buffers);
        this.aperture = aperture;
        this.focalLength = focalLength;
        this.hSamples = hSamples;
        this.vSamples = vSamples;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.ipsSeed = (long) (Long.MAX_VALUE * Math.random());
        this.essSeed = (long) (Long.MAX_VALUE * Math.random());
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

    public long getIpsSeed() {
        return ipsSeed;
    }

    public long getEssSeed() {
        return essSeed;
    }

    public float getAperture() {
        return aperture;
    }

    public float getFocalLength() {
        return focalLength;
    }
}
