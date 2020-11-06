package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clSetKernelArg;

public class PathTracingKernel extends AbstractOpenCLKernel<PathTracingKernelParams> {
    public static final String KERNEL_NAME = "path_tracing";

    private long seed0;
    private long seed1;

    public PathTracingKernel(cl_context context, long seed0, long seed1) {
        super(context);
        this.seed0 = seed0;
        this.seed1 = seed1;
    }

    public PathTracingKernel(cl_context context) {
        this(context, 0, 0);
        seed();
    }

    public void seed0(long newSeed) {
        this.seed0 = newSeed;
        setParams(getParams());
    }

    public void seed1(long newSeed) {
        this.seed1 = newSeed;
        setParams(getParams());
    }

    public void seed() {
        this.seed0 = (long) (Long.MAX_VALUE * Math.random());
        this.seed1 = (long) (Long.MAX_VALUE * Math.random());
        setParams(getParams());
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected void configureKernel(cl_kernel kernel) {
//        __kernel void path_tracing(
//                __global float4 *ray_origins,
//                __global float4 *ray_dirs,
//
//                __global const float4 *hit_normals,
//                __global const float *hit_dist,
//                __global const int *hit_map,
//
//                __global const float4 *mat_kd,
//                __global const float4 *mat_emission,
//
//                const ulong seed0,     // random seed for eye space sampling
//                const ulong seed1,     // random seed for image plane sampling
//
//                __global float4 *diffuse,
//                __global float4 *color);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayDirections()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitMap()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getDiffusePropsBuffers().getMatKd()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getDiffusePropsBuffers().getMatEmission()));

        clSetKernelArg(kernel, a++, Sizeof.cl_ulong, Pointer.to(new long[]{seed0}));
        clSetKernelArg(kernel, a++, Sizeof.cl_ulong, Pointer.to(new long[]{seed1}));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getDiffuseBuffer().getImage()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getImageBuffer().getImage()));
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getRaysBuffers().getRays()};
    }
}
