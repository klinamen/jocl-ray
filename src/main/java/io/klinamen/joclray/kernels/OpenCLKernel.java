package io.klinamen.joclray.kernels;

public interface OpenCLKernel<TParams> extends OpenCLOperation {
    void setParams(TParams kernelParams);
}
