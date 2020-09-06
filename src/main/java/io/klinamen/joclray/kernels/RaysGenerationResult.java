package io.klinamen.joclray.kernels;

import io.klinamen.joclray.FloatVec4;

public class RaysGenerationResult {
    private final float[] rayOrigins;
    private final float[] rayDirections;
    private final int rays;

    public RaysGenerationResult(int rays) {
        this.rays = rays;
        this.rayOrigins = new float[rays * FloatVec4.DIM];
        this.rayDirections = new float[rays * FloatVec4.DIM];
    }

    public RaysGenerationResult(float[] rayOrigins, float[] rayDirections) {
        if(rayDirections.length != rayOrigins.length){
            throw new IllegalArgumentException("Ray origin and directions must have the same length.");
        }

        this.rays = rayDirections.length / FloatVec4.DIM;
        this.rayOrigins = rayOrigins;
        this.rayDirections = rayDirections;
    }

    public float[] getRayOrigins() {
        return rayOrigins;
    }

    public float[] getRayDirections() {
        return rayDirections;
    }

    public int getRays() {
        return rays;
    }
}
