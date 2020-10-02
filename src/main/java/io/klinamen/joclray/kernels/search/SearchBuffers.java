package io.klinamen.joclray.kernels.search;

import io.klinamen.joclray.geom.bvh.BVNode;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.util.SortedMap;
import java.util.stream.Collectors;

import static org.jocl.CL.*;

public class SearchBuffers implements AutoCloseable {
    private final cl_mem hitMap;
    private final cl_mem bbMinVertices;
    private final cl_mem bbMaxVertices;

    private SearchBuffers(cl_mem hitMap, cl_mem bbMinVertices, cl_mem bbMaxVertices) {
        this.hitMap = hitMap;
        this.bbMinVertices = bbMinVertices;
        this.bbMaxVertices = bbMaxVertices;
    }

    public cl_mem getHitMap() {
        return hitMap;
    }

    public cl_mem getBbMinVertices() {
        return bbMinVertices;
    }

    public cl_mem getBbMaxVertices() {
        return bbMaxVertices;
    }

    public void readTo(cl_command_queue queue, SearchResult result){
        clEnqueueReadBuffer(queue, getHitMap(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_int, Pointer.to(result.getHitMap()), 0, null, null);
    }

    public static SearchBuffers create(cl_context context, SearchResult result, SortedMap<Integer, BVNode> treeIndex){
        float[] bbMinVertices = FloatVec4.flatten(treeIndex.values().stream().map(x -> x.getBoundingBox().getVertexMin())
                .collect(Collectors.toList()));

        float[] bbMaxVertices = FloatVec4.flatten(treeIndex.values().stream().map(x -> x.getBoundingBox().getVertexMax())
                .collect(Collectors.toList()));

        cl_mem mBBMinVertices = OpenCLUtils.allocateReadOnlyMem(context, bbMinVertices);
        cl_mem mBBMaxVertices = OpenCLUtils.allocateReadOnlyMem(context, bbMaxVertices);
        cl_mem hitMap = OpenCLUtils.allocateReadWriteMem(context, result.getHitMap());

        return new SearchBuffers(hitMap, mBBMinVertices, mBBMaxVertices);
    }

    @Override
    public void close() {
        clReleaseMemObject(hitMap);
        clReleaseMemObject(bbMinVertices);
        clReleaseMemObject(bbMaxVertices);
    }
}
