package io.klinamen.joclray.kernels;

import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.*;

public class ShadowRaysKernel extends AbstractOpenCLKernel<ShadowRaysKernelParams> {
    public static final String KERNEL_NAME = "shadowRays";

    private cl_mem lightPos;

    public ShadowRaysKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected cl_kernel buildKernel() {
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);

//        __kernel void shadowRays(__global float4 *rayOrigin, __global float4 *rayDirections,
//                __global float4 *hitNormals, __global float *hitDistance,
//                __global int *hitMap, __global float4 *lightPos,
//                __global float4 *shadowRayOrigin, __global float4 *shadowRayDir);

        if(lightPos != null){
            clReleaseMemObject(lightPos);
        }

        lightPos = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getLights().getFloatVec4s(x -> x.getLight().getPosition()));

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionKernelBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionKernelBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionKernelBuffers().getHitMap()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(lightPos));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getShadowRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getShadowRaysBuffers().getRayDirections()));

        return kernel;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{ getParams().getViewRaysBuffers().getRays(), getParams().getLights().size() };
    }

    @Override
    public void close() throws Exception {
        super.close();

        if(lightPos != null) {
            clReleaseMemObject(lightPos);
            lightPos = null;
        }
    }
}
