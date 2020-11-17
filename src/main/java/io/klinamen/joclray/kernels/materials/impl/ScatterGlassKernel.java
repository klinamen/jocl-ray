package io.klinamen.joclray.kernels.materials.impl;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.materials.AbstractScatterKernel;
import io.klinamen.joclray.materials.Glass;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clSetKernelArg;

public class ScatterGlassKernel extends AbstractScatterKernel {

    public ScatterGlassKernel(cl_context context) {
        super(context);
    }

    public ScatterGlassKernel(cl_context context, long seed0, long seed1) {
        super(context, seed0, seed1);
    }

    @Override
    protected String getKernelName() {
        return "scatter_glass";
    }

    @Override
    protected int configureKernel(cl_kernel kernel) {
//        __kernel void scatter_glass(
//              ...
//              __global const float4 *mat_kt,
//              __global const float4 *mat_kr,
//              __global const float *mat_ior
//        );

        ElementSet<SurfaceElement<? extends Surface>> surfaces = getParams().getScene().getSurfaces(x -> x.getMaterial() instanceof Glass);

        int a = super.configureKernel(kernel);

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(
                track(OpenCLUtils.allocateReadOnlyMem(getContext(), surfaces.getFloatVec4sById(x -> ((Glass) x.getSurface().getMaterial()).getKt())))
        ));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(
                track(OpenCLUtils.allocateReadOnlyMem(getContext(), surfaces.getFloatVec4sById(x -> ((Glass) x.getSurface().getMaterial()).getKr())))
        ));

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(
                track(OpenCLUtils.allocateReadOnlyMem(getContext(), surfaces.getFloatsById(x -> ((Glass) x.getSurface().getMaterial()).getIor())))
        ));

        return a;
    }
}
