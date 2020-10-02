package io.klinamen.joclray.kernels.intersection.factory;

import io.klinamen.joclray.geom.*;
import io.klinamen.joclray.geom.bvh.OctreeBuilder;
import io.klinamen.joclray.kernels.intersection.IntersectionKernel;
import io.klinamen.joclray.kernels.intersection.impl.*;
import io.klinamen.joclray.kernels.search.AABBTreeSearchKernel;
import org.jocl.cl_context;

import java.util.HashMap;
import java.util.Map;

public class RegistryIntersectionKernelFactory implements IntersectionKernelFactory, AutoCloseable {
    private final Map<Class<? extends Surface>, IntersectionKernel<?>> registry = new HashMap<>();

    private final AABBTreeSearchKernel aabbTreeSearchKernel;
    private final MatchingFaceSetsIntersectionKernel matchingFaceSetsIntersectionKernel;

    public RegistryIntersectionKernelFactory(cl_context context) {
        aabbTreeSearchKernel = new AABBTreeSearchKernel(context);
        matchingFaceSetsIntersectionKernel = new MatchingFaceSetsIntersectionKernel(context);

        OctreeBuilder octreeBuilder = new OctreeBuilder(2, 1);

        add(Sphere.class, new SphereIntersectionKernel(context));
        add(Plane.class, new PlaneIntersectionKernel(context));
        add(Box.class, new BoxIntersectionKernel(context));
//        add(TriangleMesh.class, new AccTriangleMeshIntersectionOperation(context, bvhBuilder, aabbTreeSearchKernel, matchingFaceSetsIntersectionKernel));
//        add(TriangleMesh.class, new TriangleMeshIntersectionKernel(context));
        add(TriangleMesh.class, new HierarchicalFaceSetIntersectionKernel(context, octreeBuilder));
    }

    private <T extends Surface> void add(Class<T> surfaceClass, IntersectionKernel<T> kernel) {
        this.registry.put(surfaceClass, kernel);
    }

    @Override
    public <T extends Surface> IntersectionKernel<T> getKernel(Class<T> surfaceClass) {
        IntersectionKernel<?> kernel = registry.get(surfaceClass);
        if (kernel == null) {
            throw new UnsupportedOperationException(String.format("Cannot find an intersection kernel. Surface class '%s' is currently not supported!", surfaceClass.getName()));
        }

        return (IntersectionKernel<T>) kernel;
    }

    @Override
    public void close() {
        aabbTreeSearchKernel.close();
        matchingFaceSetsIntersectionKernel.close();

        for (IntersectionKernel<?> k : registry.values()) {
            if (k instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) k).close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}

