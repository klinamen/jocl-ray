package io.klinamen.joclray.kernels.intersection.impl;

import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.geom.bvh.BVNode;
import io.klinamen.joclray.kernels.RaysBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.search.SearchResult;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;

import java.util.SortedMap;

public class MatchingFaceSetsIntersectionKernelParams {
    private final SortedMap<Integer, BVNode> treeIndex;
    private final SearchResult searchResult;
    private final ElementSet<SurfaceElement<TriangleMesh>> meshes;
    private final IntersectionKernelBuffers intersectionBuffers;
    private final RaysBuffers raysBuffers;

    public MatchingFaceSetsIntersectionKernelParams(IntersectionKernelBuffers intersectionBuffers, RaysBuffers raysBuffers, SortedMap<Integer, BVNode> treeIndex, SearchResult searchResult, ElementSet<SurfaceElement<TriangleMesh>> meshes) {
        this.treeIndex = treeIndex;
        this.searchResult = searchResult;
        this.meshes = meshes;
        this.intersectionBuffers = intersectionBuffers;
        this.raysBuffers = raysBuffers;
    }

    public SortedMap<Integer, BVNode> getTreeIndex() {
        return treeIndex;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public ElementSet<SurfaceElement<TriangleMesh>> getMeshes() {
        return meshes;
    }

    public IntersectionKernelBuffers getIntersectionBuffers() {
        return intersectionBuffers;
    }

    public RaysBuffers getRaysBuffers() {
        return raysBuffers;
    }
}
