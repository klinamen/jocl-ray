package io.klinamen.joclray.kernels.post;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clSetKernelArg;

public class ImageMultiplyKernel extends AbstractOpenCLKernel<ImageMultiplyKernelParams> {
    public static final String KERNEL_NAME = "image_multiply";

    public ImageMultiplyKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected int configureKernel(cl_kernel kernel) {
//        __kernel void image_multiply(const float weight, __global float *image);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{getParams().getWeight()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getImageBuffer().getImage()));

        return a;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getImageBuffer().getTotalPixels()};
    }
}
