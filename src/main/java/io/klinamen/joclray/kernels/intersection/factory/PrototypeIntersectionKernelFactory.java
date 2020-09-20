package io.klinamen.joclray.kernels.intersection.factory;

import io.klinamen.joclray.geom.*;
import io.klinamen.joclray.kernels.intersection.AbstractIntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.BoxIntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.PlaneIntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.SphereIntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.TriangleMeshIntersectionKernel;
import org.jocl.cl_context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PrototypeIntersectionKernelFactory implements IntersectionKernelFactory {
    private final Map<Class<? extends Surface>, Supplier<?>> registry = new HashMap<>();

    public PrototypeIntersectionKernelFactory(cl_context context) {
        add(Sphere.class, () -> new SphereIntersectionKernel(context));
        add(Plane.class, () -> new PlaneIntersectionKernel(context));
        add(Box.class, () -> new BoxIntersectionKernel(context));
        add(TriangleMesh.class, () -> new TriangleMeshIntersectionKernel(context));
    }

    private <T extends Surface> void add(Class<T> surfaceClass, Supplier<AbstractIntersectionKernel<T>> kernel) {
        this.registry.put(surfaceClass, kernel);
    }

    @Override
    public <T extends Surface> AbstractIntersectionKernel<T> getKernel(Class<T> surfaceClass) {
        Supplier<?> kernelSupplier = registry.get(surfaceClass);
        if (kernelSupplier == null) {
            throw new UnsupportedOperationException(String.format("Cannot find an intersection kernel. Surface class '%s' is currently not supported!", surfaceClass.getName()));
        }

        return (AbstractIntersectionKernel<T>) kernelSupplier.get();
    }
}
