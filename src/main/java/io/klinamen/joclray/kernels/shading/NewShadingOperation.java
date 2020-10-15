package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.kernels.OpenCLKernel;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernelParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongMaterialPropsBuffers;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;

import java.util.LinkedList;
import java.util.Queue;

import static org.jocl.CL.clFinish;

public class NewShadingOperation implements OpenCLKernel<NewShadingOperationParams> {
    //    public static final float AIR_IOR = 1.000273f;
    public static final float WORLD_IOR = 1.0f;

    private final cl_context context;
    private final IntersectionOperation intersection;
    private final SplitRaysKernel splitRays;
    private final BlinnPhongKernel shading;

    private final int bounces;

    private NewShadingOperationParams params;

    public NewShadingOperation(cl_context context, IntersectionOperation intersection, SplitRaysKernel splitRays, BlinnPhongKernel shading, int bounces) {
        this.context = context;
        this.intersection = intersection;
        this.splitRays = splitRays;
        this.shading = shading;

        this.bounces = bounces;
    }

    private static class RayGeneration implements AutoCloseable {
        private final WeightedRaysBuffer buffer;
        private final int generation;
        private final String name;

        public RayGeneration(WeightedRaysBuffer buffer, int generation, String name) {
            this.buffer = buffer;
            this.generation = generation;
            this.name = name;
        }

        public WeightedRaysBuffer getBuffer() {
            return buffer;
        }

        public int getGeneration() {
            return generation;
        }

        public String getName() {
            return name;
        }

        @Override
        public void close() {
            buffer.close();
        }
    }

    @Override
    public void enqueue(cl_command_queue queue) {
        try (BlinnPhongMaterialPropsBuffers materialPropsBuffers = BlinnPhongMaterialPropsBuffers.create(context, params.getScene());
             TransmissionPropsBuffers transmissionPropsBuffers = TransmissionPropsBuffers.create(context, params.getScene())
        ) {
            int iRay = 0;

            Queue<RayGeneration> qShading = new LinkedList<>();
            qShading.add(new RayGeneration(WeightedRaysBuffer.from(context, params.getViewRaysBuffer(), WORLD_IOR), 0, "p" + iRay++));

            IntersectionKernelBuffers ib = params.getIntersectionBuffers();

            while (!qShading.isEmpty()) {
                try (RayGeneration rayGen = qShading.poll()) {
                    WeightedRaysBuffer raysBuffer = rayGen.getBuffer();

                    System.out.printf("*** Processing (%s), gen %d/%d ***" + System.lineSeparator(), rayGen.getName(), rayGen.getGeneration(), bounces);

                    if (rayGen.getGeneration() > 0) {
                        if(ib != params.getIntersectionBuffers()){
                            ib.close();
                        }
                        ib = IntersectionKernelBuffers.empty(context, raysBuffer.getRaysBuffers().getRays());

                        // intersection test skipped for primary rays, as the intersection buffer already contains the result
                        intersection.setParams(new IntersectionOperationParams(params.getScene().getSurfaces(), raysBuffer.getRaysBuffers(), ib));
                        intersection.enqueue(queue);
                    }

                    shading.setParams(new BlinnPhongKernelParams(raysBuffer,
                            ib,
                            materialPropsBuffers,
                            params.getLightingBuffers(),
                            params.getImageBuffer(),
                            params.getScene().getAmbientLightIntensity(),
                            params.getScene().getLightElements().size())
                    );
                    shading.enqueue(queue);

                    if (rayGen.getGeneration() < bounces) {
                        WeightedRaysBuffer reflectedRaysBuffer = WeightedRaysBuffer.empty(context, raysBuffer.getRaysBuffers().getRays());
                        WeightedRaysBuffer transmittedRaysBuffer = WeightedRaysBuffer.empty(context, raysBuffer.getRaysBuffers().getRays());
                        splitRays.setParams(new SplitRaysKernelParams(raysBuffer, ib, transmissionPropsBuffers, reflectedRaysBuffer, transmittedRaysBuffer));
                        splitRays.enqueue(queue);

                        qShading.add(new RayGeneration(reflectedRaysBuffer, rayGen.getGeneration() + 1, String.join(",", rayGen.getName(), "r" + iRay++)));
                        qShading.add(new RayGeneration(transmittedRaysBuffer, rayGen.getGeneration() + 1, String.join(",", rayGen.getName(), "t" + iRay++)));
                    }

                    clFinish(queue);
                }
            }
        }
    }

    @Override
    public void setParams(NewShadingOperationParams kernelParams) {
        this.params = kernelParams;
    }
}
