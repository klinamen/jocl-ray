package io.klinamen.joclray.kernels.shading.blinnphong;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clSetKernelArg;

public class BlinnPhongKernel extends AbstractOpenCLKernel<BlinnPhongKernelParams> {
    public static final String KERNEL_NAME = "dl_blinn_phong";

    public BlinnPhongKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected cl_kernel buildKernel() {
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);

//        __kernel void dl_blinn_phong(
//                __global const float4 *ray_origins,
//                __global const float4 *ray_dirs,
//                __global const float4 *ray_weights,
//
//                __global const float4 *hit_normals,
//                __global const float *hit_dist,
//                __global const int *hit_map,
//
//                __global const float4 *mat_kd,
//                __global const float4 *mat_ks,
//                __global const float *mat_ph,
//
//                const uint n_lights,
//                const float amb_light_int,
//                __global const float4 *light_pos,
//                __global const float *light_int_map,
//                __global const float4 *light_dirs,
//                __global const float *light_angle,
//                __global const float *light_fallout,
//
//                __global float4 *colors);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffer().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffer().getRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysBuffer().getRayWeights()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysIntersectionBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysIntersectionBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getViewRaysIntersectionBuffers().getHitMap()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getMaterialPropsBuffers().getKd()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getMaterialPropsBuffers().getKs()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getMaterialPropsBuffers().getPhongExp()));

        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{getParams().getTotalLights()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getAmbientLightIntensity()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getLightingBuffers().getLightPos()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getLightingBuffers().getLightIntensity()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getLightingBuffers().getLightDirection()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getLightingBuffers().getLightAngleRad()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getLightingBuffers().getLightFallout()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getImageBuffer().getImage()));

        return kernel;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{ getParams().getViewRaysBuffer().getRaysBuffers().getRays() };
    }
}


