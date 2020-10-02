package io.klinamen.joclray.kernels.intersection.impl;

import com.google.common.primitives.Ints;
import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.geom.bvh.BVNode;
import io.klinamen.joclray.geom.bvh.Face;
import io.klinamen.joclray.geom.bvh.FaceSet;
import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clSetKernelArg;

public class MatchingFaceSetsIntersectionKernel extends AbstractOpenCLKernel<MatchingFaceSetsIntersectionKernelParams> {
    static class FaceSetOffset {
        public final FaceSet faceSet;
        public final int offset;

        public FaceSetOffset(FaceSet faceSet, int offset) {
            this.faceSet = faceSet;
            this.offset = offset;
        }
    }

    public MatchingFaceSetsIntersectionKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return "triangleFaceSetIntersect";
    }

    @Override
    public void setParams(MatchingFaceSetsIntersectionKernelParams kernelParams) {
        // TODO hacky
        releaseTrackedBuffers();
        super.setParams(kernelParams);
    }

    @Override
    protected cl_kernel buildKernel() {
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);

        Map<TriangleMesh, Integer> meshVerticesOffset = new HashMap<>();
        Map<TriangleMesh, Integer> meshIds = new HashMap<>();

        List<FloatVec4> mergedVertices = new ArrayList<>();
        List<FloatVec4> mergedVertexNormals = new ArrayList<>();

        int verticesOffset = 0;
        for (SurfaceElement<TriangleMesh> meshEl : getParams().getMeshes().getElements()) {
            TriangleMesh mesh = meshEl.getSurface();

            meshIds.put(mesh, meshEl.getId());

            // merge vertices
            mergedVertices.addAll(mesh.getVertices());
            mergedVertexNormals.addAll(mesh.getNormals());

            meshVerticesOffset.put(mesh, verticesOffset);
            verticesOffset = mergedVertices.size();
        }

        List<Integer> faceIndices = new ArrayList<>();
        List<Integer> faceSetToElementId = new ArrayList<>();

        Map<Integer, FaceSetOffset> faceSetIndexToOffset = new HashMap<>();

        int[] rayToTreeIndex = getParams().getSearchResult().getHitMap();
        int[] rayToFaceSetOffset = new int[rayToTreeIndex.length];
        int[] rayToFaceSetSize = new int[rayToTreeIndex.length];

        int faceIndexOffset = 0;
        for (int i = 0; i < rayToTreeIndex.length; i++) {
            int faceSetIndex = rayToTreeIndex[i];
            if(faceSetIndex < 0){
                // ray missed all bbs
                rayToFaceSetOffset[i] = -1;
                continue;
            }

            FaceSetOffset faceSetOffset = faceSetIndexToOffset.get(faceSetIndex);
            if(faceSetOffset != null){
                // faceset was already indexed
                rayToFaceSetOffset[i] = faceSetOffset.offset;
                rayToFaceSetSize[i] = faceSetOffset.faceSet.size();
                continue;
            }

            BVNode node = getParams().getTreeIndex().get(faceSetIndex);

            for (BVNode leaf : node.getChildren()) {
                FaceSet hitFaceSet = leaf.getElement();
                if(hitFaceSet == null){
                    // ray hit an empty leaf bb
                    rayToFaceSetOffset[i] = -1;
                    continue;
                }

                for (Face f : hitFaceSet.getFaces()) {
                    int meshOffset = meshVerticesOffset.get(f.getMesh());
                    for (int fi : f.getVertexIndices()) {
                        faceIndices.add(fi + meshOffset);
                        faceSetToElementId.add(meshIds.get(f.getMesh()));
                    }
                }

                rayToFaceSetOffset[i] = faceIndexOffset;
                rayToFaceSetSize[i] = hitFaceSet.size();

                faceSetIndexToOffset.put(faceSetIndex, new FaceSetOffset(hitFaceSet, faceIndexOffset));

                faceIndexOffset = faceIndices.size();
            }
        }

//        __kernel void triangleFaceSetIntersect(
//                __global const float4 *rayOrigins, __global const float4 *rayDirections,
//                __global float4 *hitNormals, __global float *hitDistance, __global int *hitMap,
//                __global const int *rayToFaceSet,
//                __global const int *rayToFaceSetSize,
//                __global const int *elementIds, __global const float4 *vertices,
//                __global const int *faceIndices, __global const float4 *vertexNormals);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitMap()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), rayToFaceSetOffset))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), rayToFaceSetSize))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(faceSetToElementId)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(mergedVertices)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(faceIndices)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(mergedVertexNormals)))));

        return kernel;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getRaysBuffers().getRays()};
    }
}
