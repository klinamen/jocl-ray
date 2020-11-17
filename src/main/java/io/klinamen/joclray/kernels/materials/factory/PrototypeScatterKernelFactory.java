package io.klinamen.joclray.kernels.materials.factory;

import io.klinamen.joclray.kernels.materials.ScatterKernel;
import io.klinamen.joclray.kernels.materials.impl.ScatterAshikhminShirleyKernel;
import io.klinamen.joclray.kernels.materials.impl.ScatterGlassKernel;
import io.klinamen.joclray.kernels.materials.impl.ScatterLambertianKernel;
import io.klinamen.joclray.kernels.materials.impl.ScatterProcKernel;
import io.klinamen.joclray.materials.*;
import org.jocl.cl_context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PrototypeScatterKernelFactory implements ScatterKernelFactory {
    private final static Map<Class<? extends Material>, Function<cl_context, ScatterKernel>> registry = new HashMap<>();

    public PrototypeScatterKernelFactory() {
        registry.put(AshikhminShirley.class, ScatterAshikhminShirleyKernel::new);
        registry.put(Lambertian.class, ScatterLambertianKernel::new);
        registry.put(ProceduralWood.class, ScatterProcKernel::new);
        registry.put(Glass.class, ScatterGlassKernel::new);
    }

    @Override
    public ScatterKernel getScatterKernel(cl_context context, Class<? extends Material> materialClass) {
        if (!registry.containsKey(materialClass)) {
            throw new RuntimeException(String.format("Unable to find a scatter kernel for material class %s.", materialClass));
        }

        return registry.get(materialClass).apply(context);
    }
}
