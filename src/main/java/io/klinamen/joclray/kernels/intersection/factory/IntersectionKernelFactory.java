package io.klinamen.joclray.kernels.intersection.factory;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;

public interface IntersectionKernelFactory {
    <T extends Surface> AbstractIntersectionKernel<T> getKernel(Class<T> surfaceClass);
}
