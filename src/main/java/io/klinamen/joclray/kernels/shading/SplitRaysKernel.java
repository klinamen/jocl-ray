package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clSetKernelArg;

public class SplitRaysKernel extends AbstractOpenCLKernel<SplitRaysKernelParams> {
    public static final String KERNEL_NAME = "split_rays";

    public SplitRaysKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected cl_kernel buildKernel() {
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);

//        __kernel void split_rays(
//                __global const float4 *ray_origins,
//                __global const float4 *ray_dirs,
//                __global const float4 *ray_weights, // weight of the incident rays
//                __global const float *ray_n,        // IoR of the material the incident ray is traveling through
//
//                __global const float4 *hit_normals,
//                __global const float *hit_distances,
//                __global const int *hit_map,
//
//                __global const float4 *mat_kr,  // material reflectivity (indexed by element id)
//                __global const float *mat_ior,  // material index of refraction (indexed by element id)
//
//                __global float4 *r_ray_origins, // origins of the reflected rays
//                __global float4 *r_ray_dirs,    // directions of the reflected rays
//                __global float4 *r_ray_weights, // weights of the reflected rays
//                __global float *r_ray_n,       // IoR of the material the reflected ray is traveling through
//
//                __global float4 *t_ray_origins, // origins of the transmitted rays
//                __global float4 *t_ray_dirs,    // directions of the transmitted rays
//                __global float4 *t_ray_weights,  // weights of the transmitted rays
//                __global float *t_ray_n,       // IoR of the material the transmitted ray is traveling through
//              );

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffer().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffer().getRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffer().getRayWeights()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitMap()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getTransmissionPropsBuffer().getMatKr()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getTransmissionPropsBuffer().getMatN()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getReflectedRaysBuffer().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getReflectedRaysBuffer().getRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getReflectedRaysBuffer().getRayWeights()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getTransmittedRaysBuffer().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getTransmittedRaysBuffer().getRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getTransmittedRaysBuffer().getRayWeights()));

        return kernel;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{ getParams().getRaysBuffer().getRaysBuffers().getRays() };
    }
}
