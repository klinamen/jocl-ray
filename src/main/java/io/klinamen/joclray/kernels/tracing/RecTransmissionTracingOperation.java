package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import io.klinamen.joclray.kernels.AbstractOpenCLOperation;
import io.klinamen.joclray.kernels.OpenCLKernel;
import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.shading.SplitRaysKernelParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernelParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongMaterialPropsBuffers;
import org.jocl.Pointer;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;

import static org.jocl.CL.clEnqueueFillBuffer;

public class RecTransmissionTracingOperation extends AbstractOpenCLOperation implements OpenCLKernel<TransmissionTracingOperationParams> {
    private final cl_context context;
    private final IntersectionOperation intersection;
    private final BlinnPhongKernel shading;
    private final int maxGeneration;
    private final AbstractOpenCLKernel<SplitRaysKernelParams> splitRaysDist;

    private TransmissionTracingOperationParams params;

    public RecTransmissionTracingOperation(cl_context context, IntersectionOperation intersection, AbstractOpenCLKernel<SplitRaysKernelParams> splitRaysDist, BlinnPhongKernel shading, int maxGeneration) {
        this.context = context;
        this.intersection = intersection;
        this.splitRaysDist = splitRaysDist;
        this.shading = shading;
        this.maxGeneration = maxGeneration;
    }

    @Override
    protected void doEnqueue(cl_command_queue queue) {
        try (BlinnPhongMaterialPropsBuffers materialPropsBuffers = BlinnPhongMaterialPropsBuffers.create(context, params.getScene());
             TransmissionPropsBuffers transmissionPropsBuffers = TransmissionPropsBuffers.create(context, params.getScene())
        ) {
            RaysGen viewRays = new RaysGen(WeightedRaysBuffer.from(context, params.getViewRaysBuffer()), "p");

            IntersectionKernelBuffers intersectionBuffers = params.getIntersectionBuffers();

            // apply shading to primary rays; intersection buffer already populated
            applyShading(queue, viewRays.getBuffer(), intersectionBuffers, materialPropsBuffers);

            process(queue, viewRays, intersectionBuffers, transmissionPropsBuffers, materialPropsBuffers);
        }
    }

    private void dispose(RaysGen source){
        // dispose source and associated buffer, if it is not the
        source.close();
        if(source.getBuffer().getRaysBuffers() != params.getViewRaysBuffer()){
            source.getBuffer().getRaysBuffers().close();
        }
    }

    protected void process(cl_command_queue queue, RaysGen source, IntersectionKernelBuffers intersectionKernelBuffers, TransmissionPropsBuffers transmissionPropsBuffers, BlinnPhongMaterialPropsBuffers materialPropsBuffers) {
        System.out.println(String.format("*** Processing (%s), gen %d/%d ***", source.getName(), source.getGeneration(), maxGeneration));

        if (source.getGeneration() >= maxGeneration) {
            dispose(source);
            return;
        }

        RaysGen reflectedRays = source.derive(context, "r");
        RaysGen transmittedRays = source.derive(context, "t");

        splitRaysDist.setParams(new SplitRaysKernelParams(
                source.getBuffer(),
                intersectionKernelBuffers,
                transmissionPropsBuffers,
                reflectedRays.getBuffer(),
                transmittedRays.getBuffer()
        ));
        splitRaysDist.enqueue(queue);

        // dispose source and associated buffer prior to process children
        dispose(source);

        intersect(queue, reflectedRays.getBuffer().getRaysBuffers(), intersectionKernelBuffers);
        applyShading(queue, reflectedRays.getBuffer(), intersectionKernelBuffers, materialPropsBuffers);

        intersect(queue, transmittedRays.getBuffer().getRaysBuffers(), intersectionKernelBuffers);
        applyShading(queue, transmittedRays.getBuffer(), intersectionKernelBuffers, materialPropsBuffers);

        process(queue, reflectedRays, intersectionKernelBuffers, transmissionPropsBuffers, materialPropsBuffers);
        process(queue, transmittedRays, intersectionKernelBuffers, transmissionPropsBuffers, materialPropsBuffers);
    }

    protected void intersect(cl_command_queue queue, RaysBuffers raysBuffers, IntersectionKernelBuffers ib){
        // reset hitmap
        clEnqueueFillBuffer(queue, ib.getHitMap(), Pointer.to(new int[]{-1}), 1, 0, params.getImageBuffer().getSize(), 0, null, null);

        intersection.setParams(new IntersectionOperationParams(params.getScene().getSurfaces(), raysBuffers, ib));
        intersection.enqueue(queue);
    }

    protected void applyShading(cl_command_queue queue, WeightedRaysBuffer raysBuffer, IntersectionKernelBuffers ib, BlinnPhongMaterialPropsBuffers materialPropsBuffers){
        shading.setParams(new BlinnPhongKernelParams(
                raysBuffer,
                ib,
                materialPropsBuffers,
                params.getLightingBuffers(),
                params.getImageBuffer(),
                params.getScene().getAmbientLightIntensity(),
                params.getScene().getLightElements().size())
        );
        shading.enqueue(queue);
    }

    @Override
    public void setParams(TransmissionTracingOperationParams kernelParams) {
        this.params = kernelParams;
    }
}
