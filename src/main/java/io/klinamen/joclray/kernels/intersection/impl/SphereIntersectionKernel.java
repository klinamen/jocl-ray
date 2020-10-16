package io.klinamen.joclray.kernels.intersection.impl;

import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.clSetKernelArg;

public class SphereIntersectionKernel extends AbstractIntersectionKernel<Sphere> {
    public SphereIntersectionKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return "sphereIntersect";
    }

    @Override
    protected void setAdditionalKernelArgs(int i, cl_kernel kernel) {
        cl_mem idsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getIds());
        cl_mem centersMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getCenter()));
        cl_mem radiusesMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloats(x -> x.getSurface().getRadius()));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(idsMem)));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(centersMem)));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(track(radiusesMem)));
    }
}
