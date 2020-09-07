package io.klinamen.joclray.kernels.intersection.impl;

import com.google.common.collect.Lists;
import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.util.List;

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
    protected List<cl_mem> setAdditionalKernelArgs(int i, cl_kernel kernel) {
        cl_mem idsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getIds());
        cl_mem planePosMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getPosition()));
        cl_mem planeNormalsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getNormal()));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(idsMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(planePosMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(planeNormalsMem));

        return Lists.newArrayList(idsMem, planePosMem, planeNormalsMem);
    }
}
