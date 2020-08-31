package io.klinamen.joclray.raycasting;

import com.google.common.collect.Lists;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;
import org.jocl.*;

import java.util.List;

import static org.jocl.CL.clSetKernelArg;

public class SphereRayCaster extends RayCaster<Sphere> {
    public SphereRayCaster(cl_context context, cl_command_queue queue) {
        super(context, queue);
    }

    @Override
    protected String getKernelName() {
        return "sphereIntersect";
    }

    @Override
    protected List<cl_mem> setAdditionalKernelArgs(int i, ElementSet<SurfaceElement<Sphere>> elements, cl_kernel kernel) {
        cl_mem idsMem = createInputBuf(elements.getIds());
        cl_mem centersMem = createInputBuf(elements.getFloatVec4s(x -> x.getSurface().getCenter()));
        cl_mem radiusesMem = createInputBuf(elements.getFloats(x -> x.getSurface().getRadius()));

        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(idsMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(centersMem));
        clSetKernelArg(kernel, i++, Sizeof.cl_mem, Pointer.to(radiusesMem));

        return Lists.newArrayList(idsMem, centersMem, radiusesMem);
    }
}
