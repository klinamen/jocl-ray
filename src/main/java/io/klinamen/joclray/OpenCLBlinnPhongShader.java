package io.klinamen.joclray;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.LightElement;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;
import org.jocl.*;

import static org.jocl.CL.*;

public class OpenCLBlinnPhongShader extends OpenCLRenderStage {
    public OpenCLBlinnPhongShader(cl_context context, cl_command_queue queue) {
        super(context, queue);
    }

    public void process(Scene scene, IntersectResult intersectResult, float[] colorsOut){
        final int rays = intersectResult.getRays();

        cl_program program = OpenCLUtils.getProgramForKernel(getContext(), "blinnPhongShading");
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kBlinnPhongShading = clCreateKernel(program, "blinnPhongShading", null);

        ElementSet<SurfaceElement<Surface>> surfaces = scene.getSurfaces();
        cl_mem kdsMem = createInputBuf(surfaces.getFloatVec4sById(x -> x.getSurface().getKd()));
        cl_mem kssMem = createInputBuf(surfaces.getFloatVec4sById(x -> x.getSurface().getKs()));
        cl_mem phongExpsMem = createInputBuf(surfaces.getFloatsById(x -> x.getSurface().getPhongExp()));

        ElementSet<LightElement> lights = scene.getLightElements();
        cl_mem lightsPosMem = createInputBuf(lights.getFloatVec4s(x -> x.getLight().getPosition()));
        cl_mem lightsIntMem = createInputBuf(lights.getFloats(x -> x.getLight().getIntensity()));

        cl_mem hitMapMem = createInputBuf(intersectResult.getHitMap());
        cl_mem normalsMem = createInputBuf(intersectResult.getHitNormals());

        cl_mem colorsOutMem = clCreateBuffer(getContext(),
                CL_MEM_WRITE_ONLY,
                Sizeof.cl_float4 * rays, null, null);

//        __kernel void
//        blinnPhongShading(__global const int *hitMap,
//                __global const float4 *normals,
//                __global const float4 *kds,
//                __global const float4 *kss,
//                __global const float *phongExp,
//                __global const float4 *lightPos,
//                __global const float *lightIntensity, const uint nLights,
//                  const float4 e, __global __write_only float4 *colors);

        int a = 0;
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(hitMapMem));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(normalsMem));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(kdsMem));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(kssMem));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(phongExpsMem));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(lightsPosMem));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(lightsIntMem));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_uint, Pointer.to(new int[]{lights.size()}));
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_float4, Pointer.to(scene.getCamera().getFrom().getArray()));

        // output
        clSetKernelArg(kBlinnPhongShading, a++, Sizeof.cl_mem, Pointer.to(colorsOutMem));

        // Set the work-item dimensions
        long[] global_work_size = new long[]{rays};

        // Execute the kernel
        clEnqueueNDRangeKernel(getQueue(), kBlinnPhongShading, 1, null,
                global_work_size, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(getQueue(), colorsOutMem, CL_TRUE, 0,
                rays * Sizeof.cl_float4, Pointer.to(colorsOut), 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(hitMapMem);
        clReleaseMemObject(normalsMem);
        clReleaseMemObject(kdsMem);
        clReleaseMemObject(kssMem);
        clReleaseMemObject(phongExpsMem);
        clReleaseMemObject(lightsPosMem);
        clReleaseMemObject(lightsIntMem);
        clReleaseMemObject(colorsOutMem);

        clReleaseKernel(kBlinnPhongShading);
        clReleaseProgram(program);

//        // DEBUG
//        int count = 200;
//        int iMin = -1;
//        for (int i = 0; i < normals.length; i++) {
//            float n = normals[i];
//            if (iMin < 0 && n > 0) {
//                iMin = i;
//            }
//
//            if (iMin > 0 && i >= iMin && (i - iMin < count)) {
//                int index = i/FloatVec4.DIM;
//                int x = index % (int) scene.getCamera().getFrameWidth();
//                int y = index / (int) scene.getCamera().getFrameWidth();
//                String com = i % 4 == 0 ? "x" : i % 4 == 1 ? "y" : i % 4 == 2 ? "z" : "w";
//                System.out.printf("%s (%s,%s) %s = %s; index(%s)=%s%n", i, x, y, com, n, index, indexes[index]);
//            }
//        }
    }
}
