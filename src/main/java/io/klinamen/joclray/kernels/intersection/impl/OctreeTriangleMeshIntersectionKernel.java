package io.klinamen.joclray.kernels.intersection.impl;

import com.google.common.primitives.Ints;
import io.klinamen.joclray.geom.AABB;
import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.octree.BVNode;
import io.klinamen.joclray.octree.Face;
import io.klinamen.joclray.octree.OctreeBuilder;
import io.klinamen.joclray.octree.TreeUtils;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.util.ArrayList;
import java.util.List;

import static org.jocl.CL.clSetKernelArg;

public class OctreeTriangleMeshIntersectionKernel extends AbstractIntersectionKernel<TriangleMesh> {
    private static final String DEFINE_N = "N";
    private static final String DEFINE_DEPTH = "DEPTH";
    private static final String DEFINE_NODES_COUNT = "NODES_COUNT";

    private final OctreeBuilder octreeBuilder;

    public OctreeTriangleMeshIntersectionKernel(cl_context context, OctreeBuilder octreeBuilder) {
        super(context);
        this.octreeBuilder = octreeBuilder;

        addCompilerOption(OpenCLUtils.defineOption(DEFINE_N, Integer.toString(octreeBuilder.getChildrenNum())));
        addCompilerOption(OpenCLUtils.defineOption(DEFINE_DEPTH, Integer.toString(octreeBuilder.getMaxDepth())));
        addCompilerOption(OpenCLUtils.defineOption(DEFINE_NODES_COUNT, Integer.toString(octreeBuilder.getTreeSize())));
    }

    @Override
    protected String getKernelName() {
        return "octreeTriangleMeshIntersect";
    }

    @Override
    protected List<cl_mem> setAdditionalKernelArgs(int a, cl_kernel kernel) {
        // one item per tree node
        List<FloatVec4> bbMinVertices = new ArrayList<>();
        List<FloatVec4> bbMaxVertices = new ArrayList<>();
        List<Integer> faceSetOffsets = new ArrayList<>();
        List<Integer> faceSetSizes = new ArrayList<>();
        List<Integer> faceSetElementId = new ArrayList<>();

        List<FloatVec4> mergedVertices = new ArrayList<>();
        List<FloatVec4> mergedVertexNormals = new ArrayList<>();
        List<Integer> facesIndices = new ArrayList<>();

        var offsets = new Object() {
            int faceSet = 0;
            int vertices = 0;
        };

        for (SurfaceElement<TriangleMesh> meshEl : getParams().getSurfaces()) {
            TriangleMesh mesh = meshEl.getSurface();

            // merge vertices
            mergedVertices.addAll(mesh.getVertices());
            mergedVertexNormals.addAll(mesh.getNormals());

            BVNode meshTree = octreeBuilder.build(mesh);

            TreeUtils.breadthFirstVisit(meshTree, n -> {
                BVNode node = n.get();
                AABB bb = node.getBoundingBox();
                bbMinVertices.add(bb.getVertexMin());
                bbMaxVertices.add(bb.getVertexMax());

                faceSetElementId.add(meshEl.getId());

                if(node.getElement() == null){
                    // non-leaf node
                    faceSetOffsets.add(-1);
                    faceSetSizes.add(0);
                } else {
                    // leaf node -> visit faces
                    for (Face f : node.getElement().getFaces()) {
                        for (int fi : f.getVertexIndices()) {
                            // vertex index translation
                            facesIndices.add(fi + offsets.vertices);
                        }
                    }

                    faceSetOffsets.add(offsets.faceSet);
                    faceSetSizes.add(node.getElement().size());

                    offsets.faceSet += node.getElement().size();
                }
            });

            offsets.vertices = mergedVertices.size();
        }

//        __kernel void
//        hierTriangleIntersect(__global const float4 *rayOrigins, __global const float4 *rayDirections,
//                __global float4 *hitNormals, __global float *hitDistances, __global int *hitMap,
//                __global const float4 *bbMinVetrices,
//                __global const float4 *bbMaxVertices,
//                __global const int *faceSetSizes,
//                __global const int *faceSetOffsets,
//                __global const int *faceIndices,
//                __global const int *faceToElementId,
//                __global const float4 *vertices,
//                __global const float4 *vertexNormals,
//        int firstLeafIndex, int nMeshes);

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(bbMinVertices)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(bbMaxVertices)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(faceSetSizes)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(faceSetOffsets)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(faceSetElementId)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(facesIndices)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(mergedVertices)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(track(OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(mergedVertexNormals)))));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{getParams().getSurfaces().size()}));

        return List.of();
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getRaysBuffers().getRays()};
    }
}
