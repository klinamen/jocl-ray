package io.klinamen.joclray.kernels.materials;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.clSetKernelArg;

public abstract class AbstractScatterKernel extends AbstractOpenCLKernel<ScatterKernelParams> implements ScatterKernel{
    private int seedArgsOffset = -1;

    private long seed0;
    private long seed1;

    public AbstractScatterKernel(cl_context context) {
        super(context);
        this.seed();
    }

    public AbstractScatterKernel(cl_context context, long seed0, long seed1) {
        super(context);
        this.seed0 = seed0;
        this.seed1 = seed1;
    }

    public void seed() {
        this.seed0 = (long) (Long.MAX_VALUE * Math.random());
        this.seed1 = (long) (Long.MAX_VALUE * Math.random());

        if(seedArgsOffset >= 0){
            // update seed kernel arguments
           setSeedArgs(getKernel(), seedArgsOffset);
        }
    }

    @Override
    protected int configureKernel(cl_kernel kernel) {
//        __kernel void scatter_<X>(
//                __global int *rayQueue,
//                __global float4 *ray_origins,
//                __global float4 *ray_dirs,
//
//                __global const float4 *hit_normals,
//                __global const float *hit_dist,
//                __global const int *hit_map,
//
//                __global const float4 *mat_emission,
//
//                const ulong seed0,     // random seed for z0
//                const ulong seed1,     // random seed for z1
//
//                __global float4 *throughput,
//                __global float4 *radiance,
//                <... material buffers ...>
//        );

        cl_mem emissionsBuf = track(OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getScene().getSurfaces().getFloatVec4sById(x -> x.getSurface().getEmission())));

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRayQueueBuffers().getQueue()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayDirections()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitMap()));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(emissionsBuf));

        seedArgsOffset = a;
        a = setSeedArgs(kernel, seedArgsOffset);

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getThroughputBuffer().getImage()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRadianceBuffer().getImage()));

        return a;
    }

    protected int setSeedArgs(cl_kernel kernel, int a){
        clSetKernelArg(kernel, a++, Sizeof.cl_ulong, Pointer.to(new long[]{seed0}));
        clSetKernelArg(kernel, a++, Sizeof.cl_ulong, Pointer.to(new long[]{seed1}));
        return a;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getQueueSize()};
    }

    @Override
    protected long[] getGlobalOffset() {
        return new long[]{getParams().getQueueOffset()};
    }
}


