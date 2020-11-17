package io.klinamen.joclray.kernels.materials.factory;

import io.klinamen.joclray.kernels.materials.ScatterKernel;
import io.klinamen.joclray.materials.Material;
import org.jocl.cl_context;

public interface ScatterKernelFactory {
    ScatterKernel getScatterKernel(cl_context context, Class<? extends Material> materialClass);
}

