package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import org.jocl.*;

import java.util.List;

import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clSetKernelArg;

public abstract class AbstractIntersectionKernel<T extends Surface> extends AbstractOpenCLKernel<IntersectionKernelParams<T>> implements IntersectionKernel<T> {
    private List<cl_mem> additionalBuffers;

    public AbstractIntersectionKernel(cl_context context) {
        super(context);
    }

    protected abstract String getKernelName();

    protected abstract List<cl_mem> setAdditionalKernelArgs(int i, cl_kernel kernel);

    @Override
    protected cl_kernel buildKernel() {
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);

//        __kernel void XIntersect(
//                const float4 rayOrigin,
//                __global float4 *rayDirections,
//                __global float4 *hitNormals,
//                __global float *hitDistance,
//                __global int *hitMap,
//                ...);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayOrigins()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRaysBuffers().getRayDirections()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitNormals()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitDistances()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitMap()));

        this.additionalBuffers = setAdditionalKernelArgs(a, kernel);

        return kernel;
    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getRaysBuffers().getRays(), getParams().getSurfaces().size()};
    }

    @Override
    public void close() {
        super.close();

        if (additionalBuffers != null) {
            additionalBuffers.forEach(CL::clReleaseMemObject);
        }
    }
}
