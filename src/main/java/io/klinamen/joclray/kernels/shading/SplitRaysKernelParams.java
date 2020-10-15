package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;

public class SplitRaysKernelParams {
    private final WeightedRaysBuffer raysBuffer;
    private final IntersectionKernelBuffers intersectionBuffers;
    private final TransmissionPropsBuffers transmissionPropsBuffer;
    private final WeightedRaysBuffer reflectedRaysBuffer;
    private final WeightedRaysBuffer transmittedRaysBuffer;

    public SplitRaysKernelParams(WeightedRaysBuffer raysBuffer, IntersectionKernelBuffers intersectionBuffers, TransmissionPropsBuffers transmissionPropsBuffer, WeightedRaysBuffer reflectedRaysBuffer, WeightedRaysBuffer transmittedRaysBuffer) {
        this.raysBuffer = raysBuffer;
        this.intersectionBuffers = intersectionBuffers;
        this.reflectedRaysBuffer = reflectedRaysBuffer;
        this.transmittedRaysBuffer = transmittedRaysBuffer;
        this.transmissionPropsBuffer = transmissionPropsBuffer;
    }

    public WeightedRaysBuffer getRaysBuffer() {
        return raysBuffer;
    }

    public IntersectionKernelBuffers getIntersectionBuffers() {
        return intersectionBuffers;
    }

    public WeightedRaysBuffer getReflectedRaysBuffer() {
        return reflectedRaysBuffer;
    }

    public WeightedRaysBuffer getTransmittedRaysBuffer() {
        return transmittedRaysBuffer;
    }

    public TransmissionPropsBuffers getTransmissionPropsBuffer() {
        return transmissionPropsBuffer;
    }
}
