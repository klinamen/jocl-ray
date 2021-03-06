package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;

public class SplitRaysDistKernelParams extends SplitRaysKernelParams {
    private final long seed;

    public SplitRaysDistKernelParams(WeightedRaysBuffer raysBuffer, IntersectionKernelBuffers intersectionBuffers, TransmissionPropsBuffers transmissionPropsBuffer, WeightedRaysBuffer reflectedRaysBuffer, WeightedRaysBuffer transmittedRaysBuffer, long seed) {
        super(raysBuffer, intersectionBuffers, transmissionPropsBuffer, reflectedRaysBuffer, transmittedRaysBuffer);
        this.seed = seed;
    }

    public SplitRaysDistKernelParams(WeightedRaysBuffer raysBuffer, IntersectionKernelBuffers intersectionBuffers, TransmissionPropsBuffers transmissionPropsBuffer, WeightedRaysBuffer reflectedRaysBuffer, WeightedRaysBuffer transmittedRaysBuffer) {
        super(raysBuffer, intersectionBuffers, transmissionPropsBuffer, reflectedRaysBuffer, transmittedRaysBuffer);
        this.seed = (long) (Long.MAX_VALUE * Math.random());
    }

    public long getSeed() {
        return seed;
    }
}
