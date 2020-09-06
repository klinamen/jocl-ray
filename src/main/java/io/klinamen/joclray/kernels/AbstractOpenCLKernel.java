package io.klinamen.joclray.kernels;

import io.klinamen.joclray.OpenCLUtils;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_program;

import static org.jocl.CL.*;

public abstract class AbstractOpenCLKernel<TParams> implements OpenCLKernel<TParams>, AutoCloseable {
    private final cl_context context;

    private cl_program program = null;
    private cl_kernel kernel = null;

    private TParams kernelParams;

    public AbstractOpenCLKernel(cl_context context) {
        this.context = context;
    }

    protected cl_context getContext() {
        return context;
    }

    @Override
    public void setParams(TParams kernelParams) {
        this.kernelParams = kernelParams;

        if(kernel != null){
            clReleaseKernel(kernel);
            kernel = null;
        }
    }

    protected TParams getParams() {
        return kernelParams;
    }

    protected final cl_program getProgram() {
        if (program == null) {
            program = OpenCLUtils.getProgramForKernel(getContext(), getKernelName());
            clBuildProgram(program, 0, null, null, null, null);
        }

        return program;
    }

    protected final cl_kernel getKernel() {
        if (kernel == null) {
            validateParams(getParams());
            kernel = buildKernel();
        }
        return kernel;
    }

    protected abstract String getKernelName();

    protected void validateParams(TParams params){

    }

    protected abstract cl_kernel buildKernel();

    protected abstract long[] getWorkgroupSize();

    public void enqueue(cl_command_queue queue) {
        // Set the work-item dimensions
        long[] global_work_size = getWorkgroupSize();

        // Execute the kernel
        clEnqueueNDRangeKernel(queue, getKernel(), global_work_size.length, null,
                global_work_size, null, 0, null, null);
    }

    @Override
    public void close() throws Exception {
        if (kernel != null) {
            clReleaseKernel(kernel);
        }

        if (program != null) {
            clReleaseProgram(program);
        }
    }
}
