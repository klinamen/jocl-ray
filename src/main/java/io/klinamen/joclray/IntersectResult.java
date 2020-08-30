package io.klinamen.joclray;

import java.util.Arrays;

public class IntersectResult {
    private final float[] hitNormals;
    private final float[] hitPoints;
    private final int[] hitMap;

    private final int rays;

    public IntersectResult(int rays) {
        this.rays = rays;

        this.hitNormals = new float[rays * FloatVec4.DIM];
        this.hitPoints = new float[rays * FloatVec4.DIM];
        this.hitMap = new int[rays];
        Arrays.fill(hitMap, -1);
    }

    public float[] getHitNormals() {
        return hitNormals;
    }

    public float[] getHitPoints() {
        return hitPoints;
    }

    public int[] getHitMap() {
        return hitMap;
    }

    public int getRays() {
        return rays;
    }
}
