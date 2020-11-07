package io.klinamen.joclray.rendering.impl;

import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.casting.RaysGenerationResult;
import io.klinamen.joclray.kernels.casting.ViewRaysKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysKernelParams;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernel;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernelParams;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.kernels.tracing.DiffusePropsBuffers;
import io.klinamen.joclray.kernels.tracing.PathTracingKernel;
import io.klinamen.joclray.kernels.tracing.PathTracingKernelParams;
import io.klinamen.joclray.rendering.AbstractOpenCLRenderer;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;
import org.jocl.Pointer;

import static org.jocl.CL.clEnqueueFillBuffer;

public class PathTracingRenderer extends AbstractOpenCLRenderer {
    private final ViewRaysKernel viewRaysKernel = new ViewRaysKernel(getContext());
    private final PathTracingKernel pathTracingKernel = new PathTracingKernel(getContext());
    private final ImageMultiplyKernel imageMultiplyKernel = new ImageMultiplyKernel(getContext());

    private final IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());
    private final IntersectionOperation intersectionOp = new IntersectionOperation(intersectionKernelFactory);

    private final int samples;
    private final int bounces;

    public PathTracingRenderer(int platformIndex, int deviceIndex, int samples, int bounces) {
        super(platformIndex, deviceIndex);
        this.samples = samples;
        this.bounces = bounces;
    }

    @Override
    protected float[] doRender(Scene scene) {
        final int nPixels = scene.getCamera().getPixels();
        float[] outImageBuf = new float[nPixels * FloatVec4.DIM];

        RaysGenerationResult raysGenerationResult = new RaysGenerationResult(nPixels);

        try (RaysBuffers viewRaysBuffers = RaysBuffers.create(getContext(), raysGenerationResult);
             ImageBuffer outImageBuffer = ImageBuffer.create(getContext(), outImageBuf);
             DiffusePropsBuffers diffusePropsBuffers = DiffusePropsBuffers.create(getContext(), scene);
        ) {
            // generate view rays
            viewRaysKernel.setParams(new ViewRaysKernelParams(scene.getCamera().getImageWidth(), scene.getCamera().getImageHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers));
            viewRaysKernel.enqueue(getQueue());

            viewRaysBuffers.readTo(getQueue(), raysGenerationResult);

            for (int i = 0; i < samples; i++) {
                try (IntersectionKernelBuffers intersectionKernelBuffers = IntersectionKernelBuffers.empty(getContext(), raysGenerationResult.getRays());
                     RaysBuffers raysBuffers = RaysBuffers.create(getContext(), raysGenerationResult);
                     ImageBuffer diffuseBuffer = ImageBuffer.empty(getContext(), raysGenerationResult.getRays(), 1.0f);
                ) {
                    intersectionOp.setParams(new IntersectionOperationParams(
                            scene.getSurfaces(), raysBuffers, intersectionKernelBuffers
                    ));

                    pathTracingKernel.setParams(new PathTracingKernelParams(
                            raysBuffers, intersectionKernelBuffers, outImageBuffer, diffuseBuffer, diffusePropsBuffers
                    ));

                    for (int j = 0; j < bounces; j++) {
                        System.out.print(String.format("Path-tracing sample %d/%d, bounce %d/%d" + System.lineSeparator(), i + 1, samples, j + 1, bounces));

                        // clear hitmap
                        clEnqueueFillBuffer(getQueue(), intersectionKernelBuffers.getHitMap(), Pointer.to(new int[]{-1}), 1, 0, outImageBuffer.getBufferSize(), 0, null, null);

                        pathTracingKernel.seed();
                        intersectionOp.enqueue(getQueue());
                        pathTracingKernel.enqueue(getQueue());
                    }
                }
            }

            // Divide by the number of samples
            imageMultiplyKernel.setParams(new ImageMultiplyKernelParams(1.0f / samples, outImageBuffer));
            imageMultiplyKernel.enqueue(getQueue());

            outImageBuffer.readTo(getQueue(), outImageBuf);
        }

        return outImageBuf;
    }

    @Override
    public void close() {
        super.close();

        viewRaysKernel.close();
        pathTracingKernel.close();
        imageMultiplyKernel.close();

        if (intersectionKernelFactory instanceof AutoCloseable) {
            try {
                ((AutoCloseable) intersectionKernelFactory).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}