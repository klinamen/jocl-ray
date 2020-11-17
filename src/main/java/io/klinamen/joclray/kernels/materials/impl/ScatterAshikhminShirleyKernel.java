package io.klinamen.joclray.kernels.materials.impl;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.materials.AbstractScatterKernel;
import io.klinamen.joclray.materials.AshikhminShirley;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.clSetKernelArg;

public class ScatterAshikhminShirleyKernel extends AbstractScatterKernel {

    public ScatterAshikhminShirleyKernel(cl_context context) {
        super(context);
    }

    public ScatterAshikhminShirleyKernel(cl_context context, long seed0, long seed1) {
        super(context, seed0, seed1);
    }

    @Override
    protected String getKernelName() {
        return "scatter_ashikhmin_shirley";
    }

    @Override
    protected int configureKernel(cl_kernel kernel) {
//        __kernel void scatter_ashikhmin_shirley(
//                __global int *rayQueue,
//                __global float4 *ray_origins,
//                __global float4 *ray_dirs,
//
//                __global const float4 *hit_normals,
//                __global const float *hit_dist,
//                __global const int *hit_map,
//
//                __global const float4 *mat_emission,
//
//                const ulong seed0,     // random seed for z0
//                const ulong seed1,     // random seed for z1
//
//                __global float4 *throughput,
//                __global float4 *radiance,
//
//                __global const float4 *mat_kd,
//                __global const float4 *mat_kr,
//                __global const float *mat_nu,
//                __global const float *mat_nv
//        )

        ElementSet<SurfaceElement<? extends Surface>> surfaces = getParams().getScene().getSurfaces(x -> x.getMaterial() instanceof AshikhminShirley);

        cl_mem kdBuf = track(OpenCLUtils.allocateReadOnlyMem(getContext(), surfaces.getFloatVec4sById(x -> ((AshikhminShirley) x.getSurface().getMaterial()).getKd())));
        cl_mem krBuf = track(OpenCLUtils.allocateReadOnlyMem(getContext(), surfaces.getFloatVec4sById(x -> ((AshikhminShirley) x.getSurface().getMaterial()).getKr())));
        cl_mem nuBuf = track(OpenCLUtils.allocateReadOnlyMem(getContext(), surfaces.getFloatsById(x -> ((AshikhminShirley) x.getSurface().getMaterial()).getNu())));
        cl_mem nvBuf = track(OpenCLUtils.allocateReadOnlyMem(getContext(), surfaces.getFloatsById(x -> ((AshikhminShirley) x.getSurface().getMaterial()).getNv())));

        int a = super.configureKernel(kernel);

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(kdBuf));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(krBuf));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(nuBuf));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(nvBuf));

        return a;
    }
}
