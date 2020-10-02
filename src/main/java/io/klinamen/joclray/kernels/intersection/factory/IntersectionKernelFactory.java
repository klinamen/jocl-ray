package io.klinamen.joclray.kernels.intersection.factory;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.intersection.IntersectionKernel;

public interface IntersectionKernelFactory {
    <T extends Surface> IntersectionKernel<T> getKernel(Class<T> surfaceClass);
}
