package io.klinamen.joclray.kernels.intersection;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.OpenCLKernel;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.SurfaceElement;
import org.jocl.cl_command_queue;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class IntersectionOperation implements OpenCLKernel<IntersectionOperationParams> {
    private final IntersectionKernelFactory factory;

    private Map<Class<? extends Surface>, ElementSet<SurfaceElement<Surface>>> surfacesGroups;
    private IntersectionOperationParams params;

    private final Map<Class<? extends Surface>, IntersectionKernel<?>> kernelsCache = new HashMap<>();

    public IntersectionOperation(IntersectionKernelFactory factory) {
        this.factory = factory;
    }

    @Override
    public void setParams(IntersectionOperationParams kernelParams) {
        this.params = kernelParams;
        this.surfacesGroups = buildSurfaceGroups();
        this.kernelsCache.clear();
    }

    private Map<Class<? extends Surface>, ElementSet<SurfaceElement<Surface>>> buildSurfaceGroups() {
        Map<Class<? extends Surface>, SortedMap<Integer, SurfaceElement<Surface>>> result = new HashMap<>();

        this.params.getSurfaces()
                .forEach(el -> {
                    Class<? extends Surface> surfaceClass = el.getSurface().getClass();
                    result.computeIfAbsent(surfaceClass, x -> new TreeMap<>())
                            .put(el.getId(), el);
                });

        return result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, x -> new ElementSet<>(x.getValue())));
    }

    private IntersectionKernel<?> getKernel(Class<? extends Surface> surfaceType) {
        // TODO review
//        if(!kernelsCache.containsKey(surfaceType)){
        ElementSet<SurfaceElement<Surface>> surfaces = surfacesGroups.get(surfaceType);
        IntersectionKernel<?> kernel = factory.getKernel(surfaceType);
        IntersectionKernelParams kernelParams = new IntersectionKernelParams(surfaces, this.params.getIntersectionKernelBuffers(), this.params.getRaysBuffers());
        kernel.setParams(kernelParams);

//            kernelsCache.put(surfaceType, kernel);
//        }

//        return kernelsCache.get(surfaceType);

        return kernel;
    }

    @Override
    public void enqueue(cl_command_queue queue) {
        if (params == null) {
            throw new RuntimeException("Operation params cannot be null.");
        }

        for (Class<? extends Surface> surfaceType : surfacesGroups.keySet()) {
            getKernel(surfaceType).enqueue(queue);
        }
    }
}

