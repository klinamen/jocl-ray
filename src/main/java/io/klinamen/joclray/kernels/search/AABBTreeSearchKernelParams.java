package io.klinamen.joclray.kernels.search;

import io.klinamen.joclray.geom.bvh.BVNode;
import io.klinamen.joclray.kernels.RaysBuffers;

public class AABBTreeSearchKernelParams {
    private final RaysBuffers viewRaysBuffers;
    private final SearchBuffers searchBuffers;
    private final BVNode tree;
    private final int level;

    public AABBTreeSearchKernelParams(RaysBuffers viewRaysBuffers, SearchBuffers searchBuffers, BVNode tree, int level) {
        this.viewRaysBuffers = viewRaysBuffers;
        this.searchBuffers = searchBuffers;
        this.tree = tree;
        this.level = level;
    }

    public BVNode getTree() {
        return tree;
    }

    public RaysBuffers getViewRaysBuffers() {
        return viewRaysBuffers;
    }

    public SearchBuffers getSearchBuffers() {
        return searchBuffers;
    }

    public int getLevel() {
        return level;
    }
}
