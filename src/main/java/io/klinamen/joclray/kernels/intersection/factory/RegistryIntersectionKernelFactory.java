package io.klinamen.joclray.kernels.intersection.factory;

import io.klinamen.joclray.geom.Box;
import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.BoxIntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.PlaneIntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.SphereIntersectionKernel;
import org.jocl.cl_context;

import java.util.HashMap;
import java.util.Map;

public class RegistryIntersectionKernelFactory implements IntersectionKernelFactory {
    private final Map<Class<? extends Surface>, AbstractIntersectionKernel<?>> registry = new HashMap<>();

    public RegistryIntersectionKernelFactory(cl_context context) {
        add(Sphere.class, new SphereIntersectionKernel(context));
        add(Plane.class, new PlaneIntersectionKernel(context));
        add(Box.class, new BoxIntersectionKernel(context));
    }

    private <T extends Surface> void add(Class<T> surfaceClass, AbstractIntersectionKernel<T> kernel){
        this.registry.put(surfaceClass, kernel);
    }

    @Override
    public <T extends Surface> AbstractIntersectionKernel<T> getKernel(Class<T> surfaceClass) {
        AbstractIntersectionKernel<?> kernel = registry.get(surfaceClass);
        if(kernel == null){
            throw new UnsupportedOperationException(String.format("Cannot find an intersection kernel. Surface class '%s' is currently not supported!", surfaceClass.getName()));
        }

        return (AbstractIntersectionKernel<T>) kernel;
    }
}

