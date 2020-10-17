package io.klinamen.joclray.kernels.casting;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clSetKernelArg;

public class ViewRaysKernel extends AbstractOpenCLKernel<ViewRaysKernelParams> {
    public static final String KERNEL_NAME = "viewRays";

    public ViewRaysKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected void configureKernel(cl_kernel kernel) {
//        __kernel void viewRays(const int2 imageSize, const float4 e, const float fov_rad,
//                __global float4 *origin,
//                __global float4 *direction);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_float2, Pointer.to(new float[]{getParams().getImageWidth(), getParams().getImageHeight()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_float4, Pointer.to(getParams().getViewOrigin().getArray()));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getFovRad()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getBuffers().getRayDirections()));
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{ getParams().getImageHeight(), getParams().getImageWidth() };
    }
}
