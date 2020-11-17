package io.klinamen.joclray.kernels.materials;

import io.klinamen.joclray.kernels.OpenCLKernel;

public interface ScatterKernel extends OpenCLKernel<ScatterKernelParams> {
    void seed();
    void close();
}
