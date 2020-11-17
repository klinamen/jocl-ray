package io.klinamen.joclray.kernels.materials;

import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;

public class MaterialSplitKernelParams {
    private final IntersectionKernelBuffers intersectionBuffers;
    private final RayQueueBuffers rayQueueBuffers;
    private final RaysBuffers raysBuffers;
    private final MaterialMapBuffers materialMapBuffers;

    public MaterialSplitKernelParams(IntersectionKernelBuffers intersectionBuffers, RayQueueBuffers rayQueueBuffers, RaysBuffers raysBuffers, MaterialMapBuffers materialMapBuffers) {
        this.intersectionBuffers = intersectionBuffers;
        this.rayQueueBuffers = rayQueueBuffers;
        this.raysBuffers = raysBuffers;
        this.materialMapBuffers = materialMapBuffers;
    }

    public MaterialMapBuffers getMaterialMapBuffers() {
        return materialMapBuffers;
    }

    public IntersectionKernelBuffers getIntersectionBuffers() {
        return intersectionBuffers;
    }

    public RayQueueBuffers getRayQueueBuffers() {
        return rayQueueBuffers;
    }

    public RaysBuffers getRaysBuffers() {
        return raysBuffers;
    }
}
