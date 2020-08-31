package io.klinamen.joclray.raycasting;

import org.jocl.cl_mem;

public class RayCasterBuffers {
    private final cl_mem hitNormalsOutMem;
    private final cl_mem hitPointsMem;
    private final cl_mem hitMapMem;
    private final long rays;

    public RayCasterBuffers(long rays, cl_mem hitNormalsOutMem, cl_mem hitPointsMem, cl_mem hitMapMem) {
        this.hitNormalsOutMem = hitNormalsOutMem;
        this.hitPointsMem = hitPointsMem;
        this.hitMapMem = hitMapMem;
        this.rays = rays;
    }

    public cl_mem getHitNormalsOutMem() {
        return hitNormalsOutMem;
    }

    public cl_mem getHitPointsMem() {
        return hitPointsMem;
    }

    public cl_mem getHitMapMem() {
        return hitMapMem;
    }

    public long getRays() {
        return rays;
    }
}
