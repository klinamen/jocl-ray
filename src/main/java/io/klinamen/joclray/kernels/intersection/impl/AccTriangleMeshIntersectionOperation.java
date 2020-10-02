package io.klinamen.joclray.kernels.intersection.impl;

import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.geom.bvh.BVNode;
import io.klinamen.joclray.geom.bvh.OctreeBuilder;
import io.klinamen.joclray.geom.bvh.TreeUtils;
import io.klinamen.joclray.kernels.intersection.IntersectionKernel;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelParams;
import io.klinamen.joclray.kernels.search.AABBTreeSearchKernel;
import io.klinamen.joclray.kernels.search.AABBTreeSearchKernelParams;
import io.klinamen.joclray.kernels.search.SearchBuffers;
import io.klinamen.joclray.kernels.search.SearchResult;
import io.klinamen.joclray.scene.SurfaceElement;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;

import java.util.SortedMap;
import java.util.stream.Stream;

public class AccTriangleMeshIntersectionOperation implements IntersectionKernel<TriangleMesh> {
    private final cl_context context;
    private final AABBTreeSearchKernel aabbTreeSearchKernel;
    private final MatchingFaceSetsIntersectionKernel matchingFaceSetsIntersectionKernel;
    private final OctreeBuilder octreeBuilder;

    private IntersectionKernelParams<TriangleMesh> params;

    public AccTriangleMeshIntersectionOperation(cl_context context, OctreeBuilder octreeBuilder, AABBTreeSearchKernel aabbTreeSearchKernel, MatchingFaceSetsIntersectionKernel matchingFaceSetsIntersectionKernel) {
        this.context = context;
        this.aabbTreeSearchKernel = aabbTreeSearchKernel;
        this.matchingFaceSetsIntersectionKernel = matchingFaceSetsIntersectionKernel;
        this.octreeBuilder = octreeBuilder;
    }

    @Override
    public void setParams(IntersectionKernelParams<TriangleMesh> kernelParams) {
        params = kernelParams;
    }

    @Override
    public void enqueue(cl_command_queue queue) {
        Stream<TriangleMesh> meshes = params.getSurfaces().getElements().stream()
                .map(SurfaceElement::getSurface);

        BVNode tree = octreeBuilder.build(meshes::iterator);
        SortedMap<Integer, BVNode> treeIndex = TreeUtils.bfIndexTree(tree);

        SearchResult searchResult = new SearchResult(params.getRaysBuffers().getRays());
        try (SearchBuffers searchBuffers = SearchBuffers.create(context, searchResult, treeIndex)) {
            // up to the last level before leaves
            for(int level = 0; level < octreeBuilder.getMaxDepth() + 1; level++) {
                var kp = new AABBTreeSearchKernelParams(params.getRaysBuffers(), searchBuffers, tree, level);
                aabbTreeSearchKernel.setParams(kp);
                aabbTreeSearchKernel.enqueue(queue);
            }
            searchBuffers.readTo(queue, searchResult);
        }

        var kp = new MatchingFaceSetsIntersectionKernelParams(params.getIntersectionBuffers(), params.getRaysBuffers(), treeIndex, searchResult, params.getSurfaces());
        matchingFaceSetsIntersectionKernel.setParams(kp);
        matchingFaceSetsIntersectionKernel.enqueue(queue);
    }
}
