package io.klinamen.joclray;

import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.raycasting.PlaneRayCaster;
import io.klinamen.joclray.raycasting.RayCasterBuffers;
import io.klinamen.joclray.raycasting.SphereRayCaster;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;
import org.jocl.*;

import static org.jocl.CL.*;

public class OpenCLRayCaster extends OpenCLRenderStage {
    public OpenCLRayCaster(cl_context context, cl_command_queue queue) {
        super(context, queue);
    }

    public void process(Scene scene, IntersectResult result) {
        cl_mem hitNormalsOutMem = clCreateBuffer(getContext(),
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float4 * result.getRays(), Pointer.to(result.getHitNormals()), null);

        cl_mem hitPointsMem = clCreateBuffer(getContext(),
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float4 * result.getRays(), Pointer.to(result.getHitPoints()), null);

        cl_mem hitMapMem = clCreateBuffer(getContext(),
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * result.getRays(), Pointer.to(result.getHitMap()), null);

        RayCasterBuffers buffers = new RayCasterBuffers(result.getRays(), hitNormalsOutMem, hitPointsMem, hitMapMem);

        ElementSet<SurfaceElement<Sphere>> spheres = scene.getSurfaceSetByType(Sphere.class);
        if(spheres.size() > 0) {
            new SphereRayCaster(getContext(), getQueue()).process(scene.getCamera(), spheres, buffers);
        }

        ElementSet<SurfaceElement<Plane>> planes = scene.getSurfaceSetByType(Plane.class);
        if(planes.size() > 0) {
            new PlaneRayCaster(getContext(), getQueue()).process(scene.getCamera(), planes, buffers);
        }

        // Read the output data
        clEnqueueReadBuffer(getQueue(), hitNormalsOutMem, CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float4, Pointer.to(result.getHitNormals()), 0, null, null);

        clEnqueueReadBuffer(getQueue(), hitPointsMem, CL_TRUE, 0,
                result.getRays() * Sizeof.cl_float4, Pointer.to(result.getHitPoints()), 0, null, null);

        clEnqueueReadBuffer(getQueue(), hitMapMem, CL_TRUE, 0,
                result.getRays() * Sizeof.cl_int, Pointer.to(result.getHitMap()), 0, null, null);

        clReleaseMemObject(hitMapMem);
        clReleaseMemObject(hitPointsMem);
        clReleaseMemObject(hitNormalsOutMem);
    }
}
