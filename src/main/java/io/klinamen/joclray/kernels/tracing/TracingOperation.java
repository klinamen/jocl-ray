package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import io.klinamen.joclray.kernels.AbstractOpenCLOperation;
import io.klinamen.joclray.kernels.OpenCLKernel;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernelParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongMaterialPropsBuffers;
import org.jocl.Pointer;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;

import java.util.LinkedList;
import java.util.Queue;

import static org.jocl.CL.clEnqueueFillBuffer;

public class TracingOperation extends AbstractOpenCLOperation implements OpenCLKernel<TracingOperationParams> {
    private final cl_context context;
    private final IntersectionOperation intersection;
    private final AbstractOpenCLKernel<SplitRaysKernelParams> splitRays;
    private final BlinnPhongKernel shading;

    private final int maxGeneration;

    private TracingOperationParams params;

    public TracingOperation(cl_context context, IntersectionOperation intersection, AbstractOpenCLKernel<SplitRaysKernelParams> splitRays, BlinnPhongKernel shading, int maxGeneration) {
        this.context = context;
        this.intersection = intersection;
        this.splitRays = splitRays;
        this.shading = shading;
        this.maxGeneration = maxGeneration;
    }

    @Override
    protected void doEnqueue(cl_command_queue queue) {
        try (TransmissionPropsBuffers transmissionPropsBuffers = TransmissionPropsBuffers.create(context, params.getScene());
             BlinnPhongMaterialPropsBuffers materialPropsBuffers = BlinnPhongMaterialPropsBuffers.create(context, params.getScene());
             IntersectionKernelBuffers ib = IntersectionKernelBuffers.empty(context, params.getViewRaysBuffer().getRays())
        ) {
            Queue<RaysGen> qShading = new LinkedList<>();
            qShading.add(new RaysGen(WeightedRaysBuffer.from(context, params.getViewRaysBuffer()), "p"));

            while (!qShading.isEmpty()) {
                try (RaysGen rayGen = qShading.poll()) {
                    WeightedRaysBuffer raysBuffer = rayGen.getBuffer();

                    System.out.printf("Tracing (%s), gen %d/%d ***" + System.lineSeparator(), rayGen.getName(), rayGen.getGeneration(), maxGeneration);

                    // reset hitmap
                    // TODO move to IntersectionOperation?
                    clEnqueueFillBuffer(queue, ib.getHitMap(), Pointer.to(new int[]{-1}), 1, 0, params.getImageBuffer().getBufferSize(), 0, null, null);

                    // intersection test skipped for primary rays, as the intersection buffer already contains the result
                    intersection.setParams(new IntersectionOperationParams(params.getScene().getSurfaces(), raysBuffer.getRaysBuffers(), ib));
                    intersection.enqueue(queue);

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
                        RaysGen reflectedRays = rayGen.derive(context, "r");
                        RaysGen transmittedRays = rayGen.derive(context, "t");

                        splitRays.setParams(new SplitRaysKernelParams(
                                raysBuffer,
                                ib,
                                transmissionPropsBuffers,
                                reflectedRays.getBuffer(),
                                transmittedRays.getBuffer()
                        ));
                        splitRays.enqueue(queue);

                        qShading.add(reflectedRays);
                        qShading.add(transmittedRays);
                    }

                    // TODO do it better
                    if (raysBuffer.getRaysBuffers() != params.getViewRaysBuffer()) {
                        raysBuffer.getRaysBuffers().close();
                    }
                }
            }
        }
    }

    @Override
    public void setParams(TracingOperationParams kernelParams) {
        this.params = kernelParams;
    }
}
