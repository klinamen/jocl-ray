package io.klinamen.joclray.kernels.search;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clSetKernelArg;

public class AABBTreeSearchKernel extends AbstractOpenCLKernel<AABBTreeSearchKernelParams> {
    public static final String KERNEL_NAME = "bbTreeSearch";

    public AABBTreeSearchKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected cl_kernel buildKernel() {
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);

//        bbTreeSearch(__global float4 *rayOrigins, __global float4 *rayDirections,
//                __global int *hitMap,
//                __global float4 bbMinVetrices,
//                __global float4 bbMaxVertices,
//                int nMeshes);

        int nMeshes = getParams().getTree().getNumChildren();

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getSearchBuffers().getHitMap()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getSearchBuffers().getBbMinVertices()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getSearchBuffers().getBbMaxVertices()));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{nMeshes}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{getParams().getLevel()}));

        return kernel;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{ getParams().getViewRaysBuffers().getRays() };
    }
}
