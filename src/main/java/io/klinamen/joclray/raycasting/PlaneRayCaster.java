package io.klinamen.joclray.raycasting;

import com.google.common.collect.Lists;
import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;
import org.jocl.*;

import java.util.List;

import static org.jocl.CL.clSetKernelArg;

public class PlaneRayCaster extends RayCaster<Plane> {
    public PlaneRayCaster(cl_context context, cl_command_queue queue) {
        super(context, queue);
    }

    @Override
    protected String getKernelName() {
        return "planeIntersect";
    }

    @Override
    protected List<cl_mem> setAdditionalKernelArgs(int i, ElementSet<SurfaceElement<Plane>> elements, cl_kernel kernel) {
        cl_mem idsMem = createInputBuf(elements.getIds());
        cl_mem planePosMem = createInputBuf(elements.getFloatVec4s(x -> x.getSurface().getPosition()));
        cl_mem planeNormalsMem = createInputBuf(elements.getFloatVec4s(x -> x.getSurface().getNormal()));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(idsMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(planePosMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(planeNormalsMem));

        return Lists.newArrayList(idsMem, planePosMem, planeNormalsMem);
    }
}
