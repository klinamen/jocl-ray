package io.klinamen.joclray.rendering.impl;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.casting.RaysBuffers;
import io.klinamen.joclray.kernels.casting.RaysGenerationResult;
import io.klinamen.joclray.kernels.casting.ViewRaysKernel;
import io.klinamen.joclray.kernels.casting.ViewRaysKernelParams;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.materials.*;
import io.klinamen.joclray.kernels.materials.factory.PrototypeScatterKernelFactory;
import io.klinamen.joclray.kernels.materials.factory.ScatterKernelFactory;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernel;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernelParams;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.kernels.tracing.PathTracingKernel;
import io.klinamen.joclray.materials.Material;
import io.klinamen.joclray.rendering.AbstractOpenCLRenderer;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.FloatVec4;
import org.jocl.Pointer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jocl.CL.clEnqueueFillBuffer;

public class SeparatePathTracingRenderer extends AbstractOpenCLRenderer {
    private final ViewRaysKernel viewRaysKernel = new ViewRaysKernel(getContext());
    private final PathTracingKernel pathTracingKernel = new PathTracingKernel(getContext());
    private final ImageMultiplyKernel imageMultiplyKernel = new ImageMultiplyKernel(getContext());

    private final IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());
    private final IntersectionOperation intersectionOp = new IntersectionOperation(intersectionKernelFactory);

    private final ScatterKernelFactory scatterKernelFactory = new PrototypeScatterKernelFactory();

    private final int samples;
    private final int bounces;

    public SeparatePathTracingRenderer(int platformIndex, int deviceIndex, int samples, int bounces) {
        super(platformIndex, deviceIndex);
        this.samples = samples;
        this.bounces = bounces;
    }

    @Override
    protected float[] doRender(Scene scene) {
        final int nPixels = scene.getCamera().getPixels();
        float[] outImageBuf = new float[nPixels * FloatVec4.DIM];

        RaysGenerationResult raysGenerationResult = new RaysGenerationResult(nPixels);

        List<ScatterKernel> materialKernels = new ArrayList<>();
        Map<Class<? extends Material>, Integer> materialIds = new HashMap<>();
        HashMap<Integer, Integer> elementIdToMaterialIdMap = new HashMap<>();

        ElementSet<SurfaceElement<Surface>> surfaces = scene.getSurfaces();
        for (SurfaceElement<Surface> surface : surfaces) {
            if (surface.getSurface().getMaterial() == null) {
                continue;
            }

            Class<? extends Material> materialClass = surface.getSurface().getMaterial().getClass();

            int materialId;
            if (!materialIds.containsKey(materialClass)) {
                materialId = materialIds.size();
                materialIds.put(materialClass, materialId);

                ScatterKernel scatterKernel = scatterKernelFactory.getScatterKernel(getContext(), materialClass);
                materialKernels.add(scatterKernel);
            } else {
                materialId = materialIds.get(materialClass);
            }

            elementIdToMaterialIdMap.put(surface.getId(), materialId);
        }

        try (MaterialSplitKernel materialSplitKernel = new MaterialSplitKernel(getContext(), materialKernels.size(), 128, nPixels);
             RaysBuffers viewRaysBuffers = RaysBuffers.create(getContext(), raysGenerationResult);
             ImageBuffer outImageBuffer = ImageBuffer.create(getContext(), outImageBuf);
             MaterialMapBuffers materialMapBuffers = MaterialMapBuffers.create(getContext(), scene, elementIdToMaterialIdMap);
        ) {
            // generate view rays
            viewRaysKernel.setParams(new ViewRaysKernelParams(scene.getCamera().getImageWidth(), scene.getCamera().getImageHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers));
            viewRaysKernel.enqueue(getQueue());

            viewRaysBuffers.readTo(getQueue(), raysGenerationResult);

            for (int i = 0; i < samples; i++) {
                try (IntersectionKernelBuffers intersectionKernelBuffers = IntersectionKernelBuffers.empty(getContext(), raysGenerationResult.getRays());
                     RaysBuffers raysBuffers = RaysBuffers.create(getContext(), raysGenerationResult);
                     ImageBuffer throughputBuffer = ImageBuffer.empty(getContext(), raysBuffers.getRays(), 1.0f);
                     RayQueueBuffers rayQueueBuffers = RayQueueBuffers.create(getContext(), materialKernels.size(), raysBuffers.getRays());
                ) {
                    intersectionOp.setParams(new IntersectionOperationParams(
                            scene.getSurfaces(), raysBuffers, intersectionKernelBuffers
                    ));

                    materialSplitKernel.setParams(new MaterialSplitKernelParams(
                            intersectionKernelBuffers,
                            rayQueueBuffers,
                            raysBuffers,
                            materialMapBuffers
                    ));

                    for (int j = 0; j < materialKernels.size(); j++) {
                        ScatterKernel kernel = materialKernels.get(j);
                        kernel.setParams(new ScatterKernelParams(
                                scene,
                                rayQueueBuffers,
                                raysBuffers,
                                intersectionKernelBuffers,
                                outImageBuffer,
                                throughputBuffer,
                                j * rayQueueBuffers.getQueueMaxSize(),
                                rayQueueBuffers.getQueueMaxSize()
                        ));

//                        System.out.println(String.format("%s offset: %d", kernel.getClass(), kernel.getParams().getQueueOffset()));
                    }

                    for (int j = 0; j < bounces; j++) {
                        System.out.print(String.format("Path-tracing sample %d/%d, bounce %d/%d" + System.lineSeparator(), i + 1, samples, j + 1, bounces));

                        // clear hitmap
                        clEnqueueFillBuffer(getQueue(), intersectionKernelBuffers.getHitMap(), Pointer.to(new int[]{-1}), 1, 0, outImageBuffer.getBufferSize(), 0, null, null);

                        // clear queueIndex
                        rayQueueBuffers.clearQueueIndexBuf(getQueue());

                        intersectionOp.enqueue(getQueue());

//                        long elapsed = System.nanoTime();

                        materialSplitKernel.enqueue(getQueue());

                        int[] queueIndex = new int[rayQueueBuffers.getNumQueues()];
                        rayQueueBuffers.readQueueIndex(getQueue(), queueIndex);

//                        elapsed = System.nanoTime() - elapsed;
//
//                        System.out.println(Arrays.toString(queueIndex));
//                        System.out.println(String.format("Split time: " + elapsed/1000000));

//                        elapsed = System.nanoTime();
                        for (int k = 0; k < materialKernels.size(); k++) {
                            if(queueIndex[k] > 0) {
                                ScatterKernel kernel = materialKernels.get(k);
                                kernel.seed();
                                kernel.getParams().setQueueSize(queueIndex[k]);
                                kernel.enqueue(getQueue());
                            }
                        }

//                        elapsed = System.nanoTime() - elapsed;
//                        System.out.println("Reparam: " + elapsed / 1000000);
                    }
                }
            }

            // Divide by the number of samples
            imageMultiplyKernel.setParams(new ImageMultiplyKernelParams(1.0f / samples, outImageBuffer));
            imageMultiplyKernel.enqueue(getQueue());

            outImageBuffer.readTo(getQueue(), outImageBuf);
        } finally {
            materialKernels.forEach(ScatterKernel::close);
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