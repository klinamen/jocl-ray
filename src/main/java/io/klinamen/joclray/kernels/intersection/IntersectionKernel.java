package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.OpenCLKernel;

public interface IntersectionKernel<T extends Surface> extends OpenCLKernel<IntersectionKernelParams<T>> {

}
