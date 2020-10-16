package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.AbstractOpenCLOperation;
import io.klinamen.joclray.kernels.OpenCLKernel;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.shading.SplitRaysKernel;
import io.klinamen.joclray.kernels.shading.SplitRaysKernelParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernelParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongMaterialPropsBuffers;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;

import java.util.LinkedList;
import java.util.Queue;

import static org.jocl.CL.clFinish;

public class TransmissionTracingOperation extends AbstractOpenCLOperation implements OpenCLKernel<TransmissionTracingOperationParams> {
    private final cl_context context;
    private final IntersectionOperation intersection;
    private final SplitRaysKernel splitRays;
    private final BlinnPhongKernel shading;

    private final int maxGeneration;

    private TransmissionTracingOperationParams params;

    public TransmissionTracingOperation(cl_context context, IntersectionOperation intersection, SplitRaysKernel splitRays, BlinnPhongKernel shading, int maxGeneration) {
        this.context = context;
        this.intersection = intersection;
        this.splitRays = splitRays;
        this.shading = shading;
        this.maxGeneration = maxGeneration;
    }

    private static class RaysGen implements AutoCloseable {
        private final WeightedRaysBuffer buffer;
        private final int generation;
        private final String name;

        public RaysGen(WeightedRaysBuffer buffer, int generation, String name) {
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
    protected void doEnqueue(cl_command_queue queue) {
        try (BlinnPhongMaterialPropsBuffers materialPropsBuffers = BlinnPhongMaterialPropsBuffers.create(context, params.getScene());
             TransmissionPropsBuffers transmissionPropsBuffers = TransmissionPropsBuffers.create(context, params.getScene())
        ) {
            int iRay = 0;

            Queue<RaysGen> qShading = new LinkedList<>();
            qShading.add(new RaysGen(WeightedRaysBuffer.from(context, params.getViewRaysBuffer(), params.getScene().getWorldIor()), 0, "p" + iRay++));

            IntersectionKernelBuffers ib = params.getIntersectionBuffers();

            while (!qShading.isEmpty()) {
                try (RaysGen rayGen = qShading.poll()) {
                    WeightedRaysBuffer raysBuffer = rayGen.getBuffer();

                    System.out.printf("*** Processing (%s), gen %d/%d ***" + System.lineSeparator(), rayGen.getName(), rayGen.getGeneration(), maxGeneration);

                    if (rayGen.getGeneration() > 0) {
//                        if(ib != params.getIntersectionBuffers()){
//                            ib.close();
//                        }
//                        ib = IntersectionKernelBuffers.empty(context, raysBuffer.getRaysBuffers().getRays());

                        // reset hitmap
                        CL.clEnqueueFillBuffer(queue, ib.getHitMap(), Pointer.to(new int[]{-1}), 1, 0, params.getImageBuffer().getSize(), 0, null, null);

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

                    if (rayGen.getGeneration() < maxGeneration) {
                        WeightedRaysBuffer reflectedRaysBuffer = WeightedRaysBuffer.empty(context, raysBuffer.getRaysBuffers().getRays());
                        WeightedRaysBuffer transmittedRaysBuffer = WeightedRaysBuffer.empty(context, raysBuffer.getRaysBuffers().getRays());
                        splitRays.setParams(new SplitRaysKernelParams(
                                raysBuffer,
                                ib,
                                transmissionPropsBuffers,
                                reflectedRaysBuffer,
                                transmittedRaysBuffer
                        ));
                        splitRays.enqueue(queue);

//                        reflectedRaysBuffer.migrateToHost(queue);
//                        transmittedRaysBuffer.migrateToHost(queue);

                        qShading.add(new RaysGen(reflectedRaysBuffer, rayGen.getGeneration() + 1, String.join(",", rayGen.getName(), "r" + iRay++)));
                        qShading.add(new RaysGen(transmittedRaysBuffer, rayGen.getGeneration() + 1, String.join(",", rayGen.getName(), "t" + iRay++)));
                    }

                    // block until command queue is empty before disposing raysBuffer
                    clFinish(queue);

                    // TODO do it better
                    if(raysBuffer.getRaysBuffers() != params.getViewRaysBuffer()){
                        raysBuffer.getRaysBuffers().close();
                    }
                }
            }
        }
    }

    @Override
    public void setParams(TransmissionTracingOperationParams kernelParams) {
        this.params = kernelParams;
    }
}
