package io.klinamen.joclray.kernels.intersection.impl;

import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.clSetKernelArg;

public class PlaneIntersectionKernel extends AbstractIntersectionKernel<Plane> {
    public PlaneIntersectionKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return "planeIntersect";
    }

    @Override
    protected void setAdditionalKernelArgs(int i, cl_kernel kernel) {
        cl_mem idsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getIds());
        cl_mem planePosMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getPosition()));
        cl_mem planeNormalsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getNormal()));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(idsMem)));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(planePosMem)));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(planeNormalsMem)));
    }
}
