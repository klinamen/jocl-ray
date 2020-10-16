package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueReadBuffer;

public class IntersectionKernelBuffers extends KernelBuffersPool {
    private final cl_mem hitNormals;
    private final cl_mem hitDistances;
    private final cl_mem hitMap;

    private IntersectionKernelBuffers(cl_mem hitNormals, cl_mem hitDistances, cl_mem hitMap) {
        this.hitNormals = track(hitNormals);
        this.hitDistances = track(hitDistances);
        this.hitMap = track(hitMap);
    }

    public cl_mem getHitNormals() {
        return hitNormals;
    }

    public cl_mem getHitDistances() {
        return hitDistances;
    }

    public cl_mem getHitMap() {
        return hitMap;
    }

    public static IntersectionKernelBuffers fromResult(cl_context context, IntersectResult result){
        cl_mem hitNormals = OpenCLUtils.allocateReadWriteMem(context, result.getHitNormals());
        cl_mem hitDistances = OpenCLUtils.allocateReadWriteMem(context, result.getHitDistances());
        cl_mem hitMap = OpenCLUtils.allocateReadWriteMem(context, result.getHitMap());

        return new IntersectionKernelBuffers(hitNormals, hitDistances, hitMap);
    }

    public static IntersectionKernelBuffers empty(cl_context context, int rays){
        cl_mem hitNormals = OpenCLUtils.allocateReadWriteMem(context, rays * FloatVec4.DIM, 0f);
        cl_mem hitDistances = OpenCLUtils.allocateReadWriteMem(context, rays * FloatVec4.DIM, 0f);
        cl_mem hitMap = OpenCLUtils.allocateReadWriteMem(context, rays, -1);

        return new IntersectionKernelBuffers(hitNormals, hitDistances, hitMap);
    }

    public void readTo(cl_command_queue queue, IntersectResult result){
        clEnqueueReadBuffer(queue, getHitNormals(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float4, Pointer.to(result.getHitNormals()), 0, null, null);

        clEnqueueReadBuffer(queue, getHitDistances(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float, Pointer.to(result.getHitDistances()), 0, null, null);

        clEnqueueReadBuffer(queue, getHitMap(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_int, Pointer.to(result.getHitMap()), 0, null, null);
    }
}
