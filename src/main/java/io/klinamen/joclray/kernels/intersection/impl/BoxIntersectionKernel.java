package io.klinamen.joclray.kernels.intersection.impl;

import com.google.common.collect.Lists;
import io.klinamen.joclray.geom.Box;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.util.List;

import static org.jocl.CL.clSetKernelArg;

public class BoxIntersectionKernel extends AbstractIntersectionKernel<Box> {
    public BoxIntersectionKernel(cl_context context) {
        super(context);
    }

    @Override
    protected String getKernelName() {
        return "boxIntersect";
    }

    @Override
    protected List<cl_mem> setAdditionalKernelArgs(int i, cl_kernel kernel) {
        cl_mem idsMem = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getIds());
        cl_mem boxVMin = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getVertexMin()));
        cl_mem boxVMax = OpenCLUtils.allocateReadOnlyMem(getContext(), getParams().getSurfaces().getFloatVec4s(x -> x.getSurface().getVertexMax()));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(idsMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(boxVMin));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(boxVMax));

        return Lists.newArrayList(idsMem, boxVMin, boxVMax);
    }
}
