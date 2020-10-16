package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.cl_context;
import org.jocl.cl_mem;

public class WeightedRaysBuffer extends KernelBuffersPool {
    private static final float DEFAULT_RAY_WEIGHT = 1.0f;

    private final cl_mem rayWeights;

    private final RaysBuffers raysBuffers;

    private WeightedRaysBuffer(RaysBuffers raysBuffers, cl_mem rayWeights) {
        this.raysBuffers = raysBuffers;
        this.rayWeights = track(rayWeights);
    }

    public cl_mem getRayWeights() {
        return rayWeights;
    }

    public RaysBuffers getRaysBuffers() {
        return raysBuffers;
    }

    public static WeightedRaysBuffer from(cl_context context, RaysBuffers raysBuffers, float mediumIoR) {
        return new WeightedRaysBuffer(raysBuffers,
                OpenCLUtils.allocateReadWriteMem(context, raysBuffers.getRays() * FloatVec4.DIM, DEFAULT_RAY_WEIGHT)
        );
    }

    public static WeightedRaysBuffer empty(cl_context context, int rays) {
        return new WeightedRaysBuffer(RaysBuffers.empty(context, rays),
                OpenCLUtils.allocateReadWriteMem(context, new float[rays * FloatVec4.DIM])
        );
    }
}
