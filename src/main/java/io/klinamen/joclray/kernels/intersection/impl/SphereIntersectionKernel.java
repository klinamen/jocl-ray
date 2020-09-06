package io.klinamen.joclray.kernels.intersection.impl;

import com.google.common.collect.Lists;
import io.klinamen.joclray.OpenCLUtils;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import org.jocl.*;

import java.util.List;

import static org.jocl.CL.clSetKernelArg;

public class SphereIntersectionKernel extends AbstractIntersectionKernel<Sphere> {
    public SphereIntersectionKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return "sphereIntersect2";
    }

    @Override
    protected List<cl_mem> setAdditionalKernelArgs(int i, cl_kernel kernel) {
        cl_mem idsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getIds());
        cl_mem centersMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getCenter()));
        cl_mem radiusesMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloats(x -> x.getSurface().getRadius()));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(idsMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(centersMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(radiusesMem));

        return Lists.newArrayList(idsMem, centersMem, radiusesMem);
    }
}
