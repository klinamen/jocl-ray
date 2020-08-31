package io.klinamen.joclray.raycasting;

import io.klinamen.joclray.OpenCLRenderStage;
import io.klinamen.joclray.OpenCLUtils;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.scene.Camera;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;
import org.jocl.*;

import java.util.List;

import static org.jocl.CL.*;

public abstract class RayCaster<T extends Surface> extends OpenCLRenderStage {
    public RayCaster(cl_context context, cl_command_queue queue) {
        super(context, queue);
    }

    protected abstract String getKernelName();

    protected abstract List<cl_mem> setAdditionalKernelArgs(int i, ElementSet<SurfaceElement<T>> elements, cl_kernel kernel);

    public void process(Camera camera, ElementSet<SurfaceElement<T>> elements, RayCasterBuffers buffers) {
        final long rays = buffers.getRays();

        cl_program program = OpenCLUtils.getProgramForKernel(getContext(), getKernelName());
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, getKernelName(), null);

        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_float2, Pointer.to(new float[]{camera.getFrameWidth(), camera.getFrameHeight()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_float4, Pointer.to(camera.getFrom().getArray()));
        clSetKernelArg(kernel, a++, Sizeof.cl_float, Pointer.to(new float[]{camera.getFovRad()}));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getHitNormalsOutMem()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getHitPointsMem()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(buffers.getHitMapMem()));

        List<cl_mem> moreArgs = setAdditionalKernelArgs(a, elements, kernel);

        // Set the work-item dimensions
        long[] global_work_size = new long[]{rays, elements.size()};

        // Execute the kernel
        clEnqueueNDRangeKernel(getQueue(), kernel, 2, null,
                global_work_size, null, 0, null, null);

        // Release kernel, program, and memory objects
        moreArgs.forEach(CL::clReleaseMemObject);

        clReleaseKernel(kernel);
        clReleaseProgram(program);
    }
}
