package io.klinamen.joclray.kernels.intersection.impl;

import com.google.common.primitives.Ints;
import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.util.ArrayList;
import java.util.List;

import static org.jocl.CL.clSetKernelArg;

public class TriangleMeshIntersectionKernel extends AbstractIntersectionKernel<TriangleMesh> {
    public TriangleMeshIntersectionKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return "triangleMeshIntersect";
    }

    @Override
    protected void setAdditionalKernelArgs(int i, cl_kernel kernel) {
//        __global const int *elementIds,
//                __global const float4 *vertices,
//                __global const int *faceIndices

        List<FloatVec4> mergedVertices = new ArrayList<>();
        List<FloatVec4> mergedVertexNormals = new ArrayList<>();
        List<Integer> mergedIndices = new ArrayList<>();
        List<Integer> elementIds = new ArrayList<>();

        int offset = 0;
        for (SurfaceElement<TriangleMesh> element : getParams().getSurfaces()) {
            TriangleMesh mesh = element.getSurface();

            // merge vertices
            mergedVertices.addAll(mesh.getVertices());
            mergedVertexNormals.addAll(mesh.getNormals());

            // translate indices
            for (Integer faceIndex : mesh.getFaceIndices()) {
                mergedIndices.add(faceIndex + offset);
                elementIds.add(element.getId());
            }

            offset = mesh.getVertices().size();
        }

        cl_mem idsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(elementIds));
        cl_mem vertices = OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(mergedVertices));
        cl_mem faceIndices = OpenCLUtils.allocateReadOnlyMem(getContext(), Ints.toArray(mergedIndices));
        cl_mem vertexNormals = OpenCLUtils.allocateReadOnlyMem(getContext(), FloatVec4.flatten(mergedVertexNormals));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(idsMem)));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(vertices)));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(faceIndices)));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(vertexNormals)));
    }

    @Override
    protected long[] getWorkgroupSize() {
        int faces = 0;
        for (SurfaceElement<TriangleMesh> surface : getParams().getSurfaces()) {
            faces += surface.getSurface().getTotalFaces();
        }

        return new long[]{getParams().getRaysBuffers().getRays(), faces};
    }
}
