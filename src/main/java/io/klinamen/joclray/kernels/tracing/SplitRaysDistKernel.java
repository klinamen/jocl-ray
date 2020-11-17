package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clSetKernelArg;


public class SplitRaysDistKernel extends AbstractOpenCLKernel<SplitRaysKernelParams> {
    public static final String KERNEL_NAME = "split_rays_dist";

    private final int samples;

    private final float areaSide;
    private long seed;

    public SplitRaysDistKernel(cl_context context, int samples, float areaSide, long seed) {
        super(context);
        this.areaSide = areaSide;
        this.seed = seed;
        this.samples = samples;

        addCompilerOption(OpenCLUtils.defineOption("SAMPLES", samples));
        addCompilerOption(OpenCLUtils.defineOption("SAMPLE_AREA_SIDE", areaSide));
    }

    public SplitRaysDistKernel(cl_context context, int samples, float areaSide) {
        this(context, samples, areaSide, (long) (Long.MAX_VALUE * Math.random()));
    }

    public void seed(long newSeed) {
        this.seed = newSeed;
        setParams(getParams());
    }

    public void seed() {
        seed((long) (Long.MAX_VALUE * Math.random()));
    }

    public int getSamples() {
        return samples;
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected int configureKernel(cl_kernel kernel) {
//        __kernel void split_rays_dist(
//                __global const float4 *ray_origins,
//                __global const float4 *ray_dirs,
//                __global const float4 *ray_weights, // weight of the incident rays
//
//                __global const float4 *hit_normals,
//                __global const float *hit_distances,
//                __global const int *hit_map,
//
//                __global const float4 *mat_kr,   // material reflectivity (indexed by element id)
//                __global const float *mat_n,     // material index of refraction (indexed by element id)
//
//                __global float4 *r_ray_origins,  // origins of the reflected rays
//                __global float4 *r_ray_dirs,     // directions of the reflected rays
//                __global float4 *r_ray_weights,  // weights of the reflected rays
//
//                __global float4 *t_ray_origins,  // origins of the transmitted rays
//                __global float4 *t_ray_dirs,     // directions of the transmitted rays
//                __global float4 *t_ray_weights,  // weights of the transmitted rays
//              const ulong seed                 // random seed
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

        clSetKernelArg(kernel, a++, Sizeof.cl_ulong, Pointer.to(new long[]{seed}));

        return a;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getRaysBuffer().getRaysBuffers().getRays()};
    }
}
