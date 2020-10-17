package io.klinamen.joclray.kernels.casting;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clSetKernelArg;

public class ViewRaysJitterKernel extends AbstractOpenCLKernel<ViewRaysJitterKernelParams> {
    public static final String KERNEL_NAME = "view_rays_jitter";

    public ViewRaysJitterKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected void configureKernel(cl_kernel kernel) {
//        __kernel void view_rays_jitter(const float2 frameSize, const float4 e, const float fov_rad,
//                              const ulong seed,   // random seed
//                              const int2 samples, // per-pixel samples
//                              const int2 index,   // sample index
//                            __global float4 *origins,
//                            __global float4 *directions);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_float2, Pointer.to(new float[]{getParams().getImageWidth(), getParams().getImageHeight()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_float4, Pointer.to(getParams().getViewOrigin().getArray()));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getFovRad()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_long, Pointer.to(new long[]{getParams().getSeed()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int2, Pointer.to(new int[]{getParams().gethSamples(), getParams().getvSamples()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int2, Pointer.to(new int[]{getParams().getxIndex(), getParams().getyIndex()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getBuffers().getRayDirections()));
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{ getParams().getImageHeight(), getParams().getImageWidth() };
    }
}
