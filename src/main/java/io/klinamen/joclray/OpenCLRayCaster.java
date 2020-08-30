package io.klinamen.joclray;

import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.scene.Camera;
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
        ElementSet<SurfaceElement<Sphere>> spheres = scene.getSurfaceSetByType(Sphere.class);
        if(spheres.size() > 0) {
            processSpheres(scene.getCamera(), spheres, result);
        }

//        int pixelIndex = scene.getCamera().getPixelIndex(.5f, .6f);
//        float hp1x = result.getHitPoints()[FloatVec4.DIM * pixelIndex];
//        float hp1y = result.getHitPoints()[FloatVec4.DIM * pixelIndex + 1];
//        float hp1z = result.getHitPoints()[FloatVec4.DIM * pixelIndex + 2];
//
//        double l1 = Math.sqrt(hp1x * hp1x + hp1y * hp1y + hp1z * hp1z);

        ElementSet<SurfaceElement<Plane>> planes = scene.getSurfaceSetByType(Plane.class);
        if(planes.size() > 0) {
            processPlanes(scene.getCamera(), planes, result);
        }

//        float hp2x = result.getHitPoints()[FloatVec4.DIM * pixelIndex];
//        float hp2y = result.getHitPoints()[FloatVec4.DIM * pixelIndex + 1];
//        float hp2z = result.getHitPoints()[FloatVec4.DIM * pixelIndex + 2];
//
//        double l2 = Math.sqrt(hp2x * hp2x + hp2y * hp2y + hp2z * hp2z);
    }

    private void processSpheres(Camera camera, ElementSet<SurfaceElement<Sphere>> spheres, IntersectResult result) {
        final int rays = result.getRays();

        cl_program program = OpenCLUtils.getProgramForKernel(getContext(), "sphereIntersect");
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kSphereIntersect = clCreateKernel(program, "sphereIntersect", null);

//        __kernel void sphereIntersect(const float2 frameSize, const float4 e,
//                            const float d,
//                            __global const int *sphereIds,
//                            __global const float4 *centers,
//                            __global const float *radiuses,
//                            const uint length,
//                            __global __write_only float4 *normals,
//                            __global __write_only float4 *hitPoints,
//                            __global __write_only int *hitMap);

        cl_mem idsMem = createInputBuf(spheres.getIds());
        cl_mem centersMem = createInputBuf(spheres.getFloatVec4s(x -> x.getSurface().getCenter()));
        cl_mem radiusesMem = createInputBuf(spheres.getFloats(x -> x.getSurface().getRadius()));

        cl_mem hitNormalsOutMem = clCreateBuffer(getContext(),
                CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float4 * rays, Pointer.to(result.getHitNormals()), null);

        cl_mem hitPointsMem = clCreateBuffer(getContext(),
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float4 * rays, Pointer.to(result.getHitPoints()), null);

        cl_mem hitMapMem = clCreateBuffer(getContext(),
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * rays, Pointer.to(result.getHitMap()), null);

        int a = 0;
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_float2, Pointer.to(new float[]{camera.getFrameWidth(), camera.getFrameHeight()}));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_float4, Pointer.to(camera.getFrom().getArray()));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_float, Pointer.to(new float[]{camera.getFovRad()}));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_mem, Pointer.to(idsMem));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_mem, Pointer.to(centersMem));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_mem, Pointer.to(radiusesMem));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_uint, Pointer.to(new int[]{spheres.size()}));

        // output
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_mem, Pointer.to(hitNormalsOutMem));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_mem, Pointer.to(hitPointsMem));
        clSetKernelArg(kSphereIntersect, a++, Sizeof.cl_mem, Pointer.to(hitMapMem));

        // Set the work-item dimensions
        long[] global_work_size = new long[]{rays};

        // Execute the kernel
        clEnqueueNDRangeKernel(getQueue(), kSphereIntersect, 1, null,
                global_work_size, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(getQueue(), hitNormalsOutMem, CL_TRUE, 0,
                rays * Sizeof.cl_float4, Pointer.to(result.getHitNormals()), 0, null, null);

        clEnqueueReadBuffer(getQueue(), hitPointsMem, CL_TRUE, 0,
                rays * Sizeof.cl_float4, Pointer.to(result.getHitPoints()), 0, null, null);

        clEnqueueReadBuffer(getQueue(), hitMapMem, CL_TRUE, 0,
                rays * Sizeof.cl_int, Pointer.to(result.getHitMap()), 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(idsMem);
        clReleaseMemObject(centersMem);
        clReleaseMemObject(radiusesMem);
        clReleaseMemObject(hitMapMem);
        clReleaseMemObject(hitPointsMem);
        clReleaseMemObject(hitNormalsOutMem);

        clReleaseKernel(kSphereIntersect);
        clReleaseProgram(program);
    }

    private void processPlanes(Camera camera, ElementSet<SurfaceElement<Plane>> planes, IntersectResult result) {
        final int rays = result.getRays();

        cl_program program = OpenCLUtils.getProgramForKernel(getContext(), "planeIntersect");
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kPlaneIntersect = clCreateKernel(program, "planeIntersect", null);

//        __kernel void planeIntersect(const float2 frameSize, const float4 e,
//                const float d, __global const int *elemIds,
//                __global const float4 *planePos,
//                __global const float4 *planeNormals,
//                const uint nElems,
//                __global __write_only float4 *normals,
//                __global __read_write float4 *hitPoints,
//                __global __read_write int *hitMap);

        cl_mem idsMem = createInputBuf(planes.getIds());
        cl_mem planePosMem = createInputBuf(planes.getFloatVec4s(x -> x.getSurface().getPosition()));
        cl_mem planeNormalsMem = createInputBuf(planes.getFloatVec4s(x -> x.getSurface().getNormal()));

        cl_mem hitNormalsOutMem = clCreateBuffer(getContext(),
                CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float4 * rays, Pointer.to(result.getHitNormals()), null);

        cl_mem hitPointsMem = clCreateBuffer(getContext(),
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float4 * rays, Pointer.to(result.getHitPoints()), null);

        cl_mem hitMapMem = clCreateBuffer(getContext(),
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * rays, Pointer.to(result.getHitMap()), null);

        int a = 0;
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_float2, Pointer.to(new float[]{camera.getFrameWidth(), camera.getFrameHeight()}));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_float4, Pointer.to(camera.getFrom().getArray()));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_float, Pointer.to(new float[]{camera.getFovRad()}));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_mem, Pointer.to(idsMem));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_mem, Pointer.to(planePosMem));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_mem, Pointer.to(planeNormalsMem));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_uint, Pointer.to(new int[]{planes.size()}));

        // output
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_mem, Pointer.to(hitNormalsOutMem));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_mem, Pointer.to(hitPointsMem));
        clSetKernelArg(kPlaneIntersect, a++, Sizeof.cl_mem, Pointer.to(hitMapMem));

        // Set the work-item dimensions
        long[] global_work_size = new long[]{rays};

        // Execute the kernel
        clEnqueueNDRangeKernel(getQueue(), kPlaneIntersect, 1, null,
                global_work_size, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(getQueue(), hitNormalsOutMem, CL_TRUE, 0,
                rays * Sizeof.cl_float4, Pointer.to(result.getHitNormals()), 0, null, null);

        clEnqueueReadBuffer(getQueue(), hitPointsMem, CL_TRUE, 0,
                rays * Sizeof.cl_float4, Pointer.to(result.getHitPoints()), 0, null, null);

        clEnqueueReadBuffer(getQueue(), hitMapMem, CL_TRUE, 0,
                rays * Sizeof.cl_int, Pointer.to(result.getHitMap()), 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(idsMem);
        clReleaseMemObject(planePosMem);
        clReleaseMemObject(planeNormalsMem);
        clReleaseMemObject(hitMapMem);
        clReleaseMemObject(hitPointsMem);
        clReleaseMemObject(hitNormalsOutMem);

        clReleaseKernel(kPlaneIntersect);
        clReleaseProgram(program);
    }
}
