package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clSetKernelArg;

public class ShadingKernel extends AbstractOpenCLKernel<ShadingKernelParams> {
    public static final String KERNEL_NAME = "shading";

    public ShadingKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected cl_kernel buildKernel() {
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);

//        __kernel void shading(__global float4 *rayOrigin, __global float4 *rayDirections,
//                __global float4 *hitNormals, __global float *hitDistance,
//                __global int *hitMap, const float aLightIntensity,
//                __global const float4 *kd, __global const float4 *ks,
//                __global const float4 *kr, __global const float *phongExp,
//                __global float4 *krPrev,
//                __global const float4 *lightPos,
//                __global const float *lightIntensity, const uint nLights,
//                __global float4 *colors);

        ShadingKernelBuffers buffers = getParams().getBuffers();

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffer().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffer().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysIntersectionBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysIntersectionBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysIntersectionBuffers().getHitMap()));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getAmbientLightIntensity()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getKd()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getKs()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getKr()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getPhongExp()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getKrPrev()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getLightPos()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getLightIntensity()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getLightDirection()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getLightAngleRad()));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{getParams().getTotalLights()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getImage()));

        return kernel;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{ getParams().getViewRaysBuffer().getRays() };
    }
}


