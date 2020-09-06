package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.*;

public class IntersectionKernelBuffers implements AutoCloseable {
    private final cl_mem hitNormals;
    private final cl_mem hitDistances;
    private final cl_mem hitMap;

    private IntersectionKernelBuffers(cl_mem hitNormals, cl_mem hitDistances, cl_mem hitMap) {
        this.hitNormals = hitNormals;
        this.hitDistances = hitDistances;
        this.hitMap = hitMap;
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

    public void readTo(cl_command_queue queue, IntersectResult result){
        clEnqueueReadBuffer(queue, getHitNormals(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float4, Pointer.to(result.getHitNormals()), 0, null, null);

        clEnqueueReadBuffer(queue, getHitDistances(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float, Pointer.to(result.getHitDistances()), 0, null, null);

        clEnqueueReadBuffer(queue, getHitMap(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_int, Pointer.to(result.getHitMap()), 0, null, null);
    }

    @Override
    public void close() throws Exception {
        clReleaseMemObject(hitMap);
        clReleaseMemObject(hitDistances);
        clReleaseMemObject(hitNormals);
    }
}
