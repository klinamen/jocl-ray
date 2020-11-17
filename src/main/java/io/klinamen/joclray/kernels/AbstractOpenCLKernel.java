package io.klinamen.joclray.kernels;

import io.klinamen.joclray.util.KernelBuffersPoolManager;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.util.ArrayList;
import java.util.List;

import static org.jocl.CL.*;

public abstract class AbstractOpenCLKernel<TParams> extends AbstractOpenCLOperation implements OpenCLKernel<TParams>, AutoCloseable {
    private final cl_context context;

    private cl_program program = null;
    private cl_kernel kernel = null;

    private TParams kernelParams;

    private final KernelBuffersPoolManager buffersPoolManager = new KernelBuffersPoolManager(this.getClass().getSimpleName());

    private final List<String> compilerOptions = new ArrayList<>();

    public AbstractOpenCLKernel(cl_context context) {
        this.context = context;
    }

    protected cl_context getContext() {
        return context;
    }

    protected cl_mem track(cl_mem buffer){
        return buffersPoolManager.track(buffer);
    }

    @Override
    public void setParams(TParams kernelParams) {
        this.kernelParams = kernelParams;

        buffersPoolManager.close();
        releaseKernel();
    }

    public TParams getParams() {
        return kernelParams;
    }

    protected void addCompilerOption(String option){
        compilerOptions.add(option);
        releaseProgram();
    }

    protected void clearCompilerOptions(){
        compilerOptions.clear();
        releaseProgram();
    }

    private void releaseProgram(){
        releaseKernel();

        if (program != null) {
            clReleaseProgram(program);
            program = null;
        }
    }

    private void releaseKernel(){
        if (kernel != null) {
            clReleaseKernel(kernel);
            kernel = null;
        }
    }

    protected final cl_program getProgram() {
        if (program == null) {
            program = OpenCLUtils.getProgramForKernel(getContext(), getKernelName());
            String options = String.join(" ", compilerOptions);
            clBuildProgram(program, 0, null, options, null, null);
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

    protected cl_kernel buildKernel(){
        cl_kernel kernel = clCreateKernel(getProgram(), getKernelName(), null);
        configureKernel(kernel);
        return kernel;
    }

    protected abstract int configureKernel(cl_kernel kernel);

    protected abstract long[] getWorkgroupSize();

    protected long[] getGlobalOffset(){
        return null;
    }

    protected long[] getLocalWorkSize(){
        return null;
    }

    @Override
    public void doEnqueue(cl_command_queue queue) {
        // Set the work-item dimensions
        long[] global_work_size = getWorkgroupSize();

        // Execute the kernel
        clEnqueueNDRangeKernel(queue, getKernel(), global_work_size.length, getGlobalOffset(),
                global_work_size, getLocalWorkSize(), 0, null, null);
    }

    @Override
    public void close() {
        buffersPoolManager.close();
        releaseProgram();
    }
}
