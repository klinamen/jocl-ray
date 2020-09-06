package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.FloatVec4;

import java.util.Arrays;

public class IntersectResult {
    private final float[] rayOrigins;
    private final float[] rayDirections;
    private final float[] hitNormals;
    private final float[] hitDistances;
    private final int[] hitMap;

    private final int rays;

    public IntersectResult(int rays) {
        this.rays = rays;

        this.rayOrigins = new float[rays * FloatVec4.DIM];
        this.rayDirections = new float[rays * FloatVec4.DIM];
        this.hitNormals = new float[rays * FloatVec4.DIM];
        this.hitDistances = new float[rays];

        this.hitMap = new int[rays];
        Arrays.fill(hitMap, -1);
    }

    public float[] getHitNormals() {
        return hitNormals;
    }

    public float[] getHitDistances() {
        return hitDistances;
    }

    public int[] getHitMap() {
        return hitMap;
    }

    public int getRays() {
        return rays;
    }

    public float[] getRayDirections() {
        return rayDirections;
    }

    public float[] getRayOrigins() {
        return rayOrigins;
    }
}
