package io.klinamen.joclray.kernels;

import io.klinamen.joclray.BaseKernelBuffers;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.nio.FloatBuffer;

import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueReadBuffer;

public class RaysBuffers extends BaseKernelBuffers {
    private final cl_mem rayOrigins;
    private final cl_mem rayDirections;

    private final int rays;

    protected RaysBuffers(int rays, cl_mem rayOrigins, cl_mem rayDirections) {
        this.rays = rays;
        this.rayOrigins = track(rayOrigins);
        this.rayDirections = track(rayDirections);
    }

    public cl_mem getRayOrigins() {
        return rayOrigins;
    }

    public cl_mem getRayDirections() {
        return rayDirections;
    }

    public int getRays() {
        return rays;
    }

    public void readTo(cl_command_queue queue, RaysGenerationResult result) {
        clEnqueueReadBuffer(queue, getRayOrigins(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float4, Pointer.to(result.getRayOrigins()), 0, null, null);

        clEnqueueReadBuffer(queue, getRayDirections(), CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float4, Pointer.to(result.getRayDirections()), 0, null, null);
    }

    public static RaysBuffers create(cl_context context, RaysGenerationResult result) {
        cl_mem rayOrigins = OpenCLUtils.allocateReadWriteMem(context, result.getRayOrigins());
        cl_mem rayDirections = OpenCLUtils.allocateReadWriteMem(context, result.getRayDirections());

        return new RaysBuffers(result.getRays(), rayOrigins, rayDirections);
    }

    public static RaysBuffers empty(cl_context context, int rays) {
        cl_mem rayOrigins = OpenCLUtils.allocateReadWriteMem(context, new float[rays * FloatVec4.DIM]);
        cl_mem rayDirections = OpenCLUtils.allocateReadWriteMem(context, new float[rays * FloatVec4.DIM]);
        return new RaysBuffers(rays, rayOrigins, rayDirections);
    }

    public static RaysBuffers create(cl_context context, FloatBuffer rayOriginsBuffer, FloatBuffer rayDirectionsBuffer) {
        if ((rayOriginsBuffer.limit() - rayOriginsBuffer.position()) != (rayDirectionsBuffer.limit() - rayDirectionsBuffer.position())) {
            throw new IllegalArgumentException("Buffers differ in size.");
        }

        cl_mem rayOrigins = OpenCLUtils.allocateReadOnlyMem(context, rayOriginsBuffer);
        cl_mem rayDirections = OpenCLUtils.allocateReadOnlyMem(context, rayDirectionsBuffer);

        return new RaysBuffers(rayOriginsBuffer.limit(), rayOrigins, rayDirections);
    }
}
