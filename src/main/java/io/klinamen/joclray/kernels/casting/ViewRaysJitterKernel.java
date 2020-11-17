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
    protected int configureKernel(cl_kernel kernel) {
//        __kernel void view_rays_jitter(const float2 frameSize,
//                              const float4 e,           // eye origin
//                              const float fov_rad,      // field of view angle (rad)
//                              const float aperture,     // aperture size for eye space sampling
//                              const float focal_length, // focal length
//                              const ulong ess_seed,     // random seed for eye space sampling
//                              const ulong ips_seed,     // random seed for image plane sampling
//                              const int2 ips_samples,   // per-pixel ips_samples
//                              const int2 ips_index,     // image plane sample index
//                              __global float4 *origins,
//                __global float4 *directions);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_float2, Pointer.to(new float[]{getParams().getImageWidth(), getParams().getImageHeight()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_float4, Pointer.to(getParams().getViewOrigin().getArray()));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getFovRad()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getAperture()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getFocalLength()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_long, Pointer.to(new long[]{getParams().getEssSeed()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_long, Pointer.to(new long[]{getParams().getIpsSeed()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int2, Pointer.to(new int[]{getParams().gethSamples(), getParams().getvSamples()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int2, Pointer.to(new int[]{getParams().getxIpsIndex(), getParams().getyIpsIndex()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[]{getParams().getEssIndex()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getBuffers().getRayDirections()));

        return a;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getImageHeight(), getParams().getImageWidth()};
    }
}
