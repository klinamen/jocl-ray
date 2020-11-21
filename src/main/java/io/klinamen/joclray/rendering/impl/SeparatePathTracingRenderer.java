package io.klinamen.joclray.rendering.impl;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.casting.RaysBuffers;
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
import org.jocl.Sizeof;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jocl.CL.clEnqueueCopyBuffer;
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

        final int nPixels = scene.getCamera().getPixels();
        float[] outImageBuf = new float[nPixels * FloatVec4.DIM];

        try (MaterialSplitKernel materialSplitKernel = new MaterialSplitKernel(getContext(), materialKernels.size(), 128, nPixels);
             RaysBuffers viewRaysBuffers = RaysBuffers.empty(getContext(), nPixels);
             ImageBuffer outImageBuffer = ImageBuffer.create(getContext(), outImageBuf);
             MaterialMapBuffers materialMapBuffers = MaterialMapBuffers.create(getContext(), scene, elementIdToMaterialIdMap);
             IntersectionKernelBuffers intersectionKernelBuffers = IntersectionKernelBuffers.empty(getContext(), nPixels);
             RaysBuffers raysBuffers = RaysBuffers.empty(getContext(), nPixels);
             ImageBuffer throughputBuffer = ImageBuffer.empty(getContext(), nPixels, 1.0f);
             RayQueueBuffers rayQueueBuffers = RayQueueBuffers.create(getContext(), materialKernels.size(), nPixels);
        ) {
            // configure intersectionOp
            intersectionOp.setParams(new IntersectionOperationParams(
                    scene.getSurfaces(), raysBuffers, intersectionKernelBuffers
            ));

            // configure materialSplitKernel
            materialSplitKernel.setParams(new MaterialSplitKernelParams(
                    intersectionKernelBuffers,
                    rayQueueBuffers,
                    raysBuffers,
                    materialMapBuffers
            ));

            // configure material kernels
            for (int i = 0; i < materialKernels.size(); i++) {
                ScatterKernel kernel = materialKernels.get(i);
                kernel.setParams(new ScatterKernelParams(
                        scene,
                        rayQueueBuffers,
                        raysBuffers,
                        intersectionKernelBuffers,
                        outImageBuffer,
                        throughputBuffer,
                        i * rayQueueBuffers.getQueueMaxSize(),
                        rayQueueBuffers.getQueueMaxSize()
                ));
            }

            // generate view rays
            viewRaysKernel.setParams(new ViewRaysKernelParams(scene.getCamera().getImageWidth(), scene.getCamera().getImageHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers));
            viewRaysKernel.enqueue(getQueue());

//            RaysGenerationResult res = new RaysGenerationResult(nPixels);
//            viewRaysBuffers.readTo(getQueue(), res);
//
//            float[] dirs = res.getRayDirections();
//            float[] origins = res.getRayOrigins();
//            for(int i=0; i<nPixels; i+=FloatVec4.DIM){
//                int iDest = (int)Math.round(Math.random() * nPixels);
//                for(int j=0; j<FloatVec4.DIM; j++){
//                    float tmp_dir = dirs[iDest + j];
//                    dirs[iDest + j] = dirs[i + j];
//                    dirs[i + j] = tmp_dir;
//
//                    float tmp_orig = origins[iDest + j];
//                    origins[iDest + j] = origins[i + j];
//                    origins[i + j] = tmp_orig;
//                }
//            }
//
//            clEnqueueWriteBuffer(getQueue(), viewRaysBuffers.getRayOrigins(), true, 0, Sizeof.cl_float * origins.length, Pointer.to(origins), 0, null, null);
//            clEnqueueWriteBuffer(getQueue(), viewRaysBuffers.getRayDirections(), true, 0, Sizeof.cl_float * dirs.length, Pointer.to(dirs), 0, null, null);

            for (int i = 0; i < samples; i++) {
                // reset raysBuffers to viewRays
                clEnqueueCopyBuffer(getQueue(), viewRaysBuffers.getRayOrigins(), raysBuffers.getRayOrigins(), 0, 0, Sizeof.cl_float4 * nPixels, 0, null, null);
                clEnqueueCopyBuffer(getQueue(), viewRaysBuffers.getRayDirections(), raysBuffers.getRayDirections(), 0, 0, Sizeof.cl_float4 * nPixels, 0, null, null);

                // reset throughput buffer
                clEnqueueFillBuffer(getQueue(), throughputBuffer.getImage(), Pointer.to(new float[]{1.0f}), Sizeof.cl_float , 0, Sizeof.cl_float4 * nPixels, 0, null, null);

                for (int j = 0; j < bounces; j++) {
                    System.out.print(String.format("Path-tracing sample %d/%d, bounce %d/%d" + System.lineSeparator(), i + 1, samples, j + 1, bounces));

                    // clear hitmap
                    clEnqueueFillBuffer(getQueue(), intersectionKernelBuffers.getHitMap(), Pointer.to(new int[]{-1}), Sizeof.cl_int, 0, outImageBuffer.getBufferSize(), 0, null, null);

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
                        if (queueIndex[k] > 0) {
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