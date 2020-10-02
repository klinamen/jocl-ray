package io.klinamen.joclray.kernels.search;

import java.util.Arrays;

public class SearchResult {
    private final int rays;
    private final int[] hitMap;

    public SearchResult(int rays) {
        this.rays = rays;
        this.hitMap = new int[rays];
        Arrays.fill(hitMap, -1);
    }

    public int[] getHitMap() {
        return hitMap;
    }

    public int getRays() {
        return rays;
    }
}
