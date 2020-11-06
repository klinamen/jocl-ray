package io.klinamen.joclray.rendering;

import io.klinamen.joclray.display.ShadingDisplay;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.kernels.casting.*;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernel;
import io.klinamen.joclray.kernels.post.ImageMultiplyKernelParams;
import io.klinamen.joclray.kernels.shading.ImageBuffer;
import io.klinamen.joclray.kernels.shading.LightIntensityMapOperation;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernel;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongKernelParams;
import io.klinamen.joclray.kernels.shading.blinnphong.BlinnPhongMaterialPropsBuffers;
import io.klinamen.joclray.kernels.tracing.*;
import io.klinamen.joclray.scene.*;
import io.klinamen.joclray.tonemapping.ReinhardToneMapping;
import io.klinamen.joclray.util.FloatVec4;
import org.jocl.Pointer;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jocl.CL.clEnqueueFillBuffer;

public class PathTracingRenderer extends OpenCLRenderer {
    private final ViewRaysKernel viewRaysKernel = new ViewRaysKernel(getContext());
    private final PathTracingKernel pathTracingKernel = new PathTracingKernel(getContext());
    private final ImageMultiplyKernel imageMultiplyKernel = new ImageMultiplyKernel(getContext());
    private final BlinnPhongKernel blinnPhongKernel = new BlinnPhongKernel(getContext());
    private final ShadowRaysKernel shadowRaysKernel = new ShadowRaysKernel(getContext());

    private final IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(getContext());
    private final IntersectionOperation intersectionOp = new IntersectionOperation(intersectionKernelFactory);
    private final LightIntensityMapOperation lightIntensityMapOperation = new LightIntensityMapOperation(getContext(), shadowRaysKernel, intersectionOp);

    private final int samples;
    private final int bounces;

    public PathTracingRenderer(int platformIndex, int deviceIndex, int samples, int bounces) {
        super(platformIndex, deviceIndex);
        this.samples = samples;
        this.bounces = bounces;
    }

    @Override
    protected void doRender(Scene scene, BufferedImage outImage) {
        final int nPixels = scene.getCamera().getPixels();
        float[] outImageBuf = new float[nPixels * FloatVec4.DIM];

        RaysGenerationResult raysGenerationResult = new RaysGenerationResult(nPixels);

        try (RaysBuffers viewRaysBuffers = RaysBuffers.create(getContext(), raysGenerationResult);
             ImageBuffer outImageBuffer = ImageBuffer.create(getContext(), outImageBuf);
        ) {
            // generate view rays
            viewRaysKernel.setParams(new ViewRaysKernelParams(outImage.getWidth(), outImage.getHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers));
            viewRaysKernel.enqueue(getQueue());

            viewRaysBuffers.readTo(getQueue(), raysGenerationResult);

            applyIndirectLighting(scene, raysGenerationResult, outImageBuffer);
//            applyDirectLighting(scene, viewRaysBuffers, outImageBuffer);

            outImageBuffer.readTo(getQueue(), outImageBuf);
        }

        // update image
        new ShadingDisplay(scene, outImageBuf, new ReinhardToneMapping())
                .update(outImage);
    }

    private void applyIndirectLighting(Scene scene, RaysGenerationResult raysGenerationResult, ImageBuffer outImageBuffer){
        try(DiffusePropsBuffers diffusePropsBuffers = DiffusePropsBuffers.create(getContext(), scene)) {
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
                        System.out.print(String.format("Pathtracing sample %d/%d, bounce %d/%d" + System.lineSeparator(), i + 1, samples, j + 1, bounces));

                        clEnqueueFillBuffer(getQueue(), intersectionKernelBuffers.getHitMap(), Pointer.to(new int[]{-1}), 1, 0, outImageBuffer.getBufferSize(), 0, null, null);
                        pathTracingKernel.seed();
                        intersectionOp.enqueue(getQueue());
                        pathTracingKernel.enqueue(getQueue());
                    }
                }
            }

            // Divide by the number of samples and the constant pdf=1/2pi
            imageMultiplyKernel.setParams(new ImageMultiplyKernelParams(1.0f / samples, outImageBuffer));
            imageMultiplyKernel.enqueue(getQueue());
        }
    }

    private void applyDirectLighting(Scene scene, RaysBuffers viewRaysBuffers, ImageBuffer outImageBuffer){
        try (WeightedRaysBuffer weightedRayBuffer = WeightedRaysBuffer.from(getContext(), viewRaysBuffers, 1f);
             IntersectionKernelBuffers intersectionKernelBuffers = IntersectionKernelBuffers.empty(getContext(), viewRaysBuffers.getRays());
             BlinnPhongMaterialPropsBuffers materialPropsBuffers = BlinnPhongMaterialPropsBuffers.create(getContext(), scene);
        ) {
            ElementSet<SurfaceElement<Surface>> nonEmittingSurfaces = new ElementSet<>(new TreeMap<>(scene.getSurfaces().getElements().stream()
                    .filter(x -> x.getSurface().getEmission().maxComponent() <= 0)
                    .collect(Collectors.toMap(Element::getId, Function.identity()))));

            intersectionOp.setParams(new IntersectionOperationParams(
                    nonEmittingSurfaces, viewRaysBuffers, intersectionKernelBuffers
            ));

            intersectionOp.enqueue(getQueue());

            float[] lightIntensityMap = buildLightMap(scene, viewRaysBuffers.getRays());
            try (LightingBuffers lb = LightingBuffers.create(getContext(), scene, lightIntensityMap)) {
                blinnPhongKernel.setParams(new BlinnPhongKernelParams(
                        weightedRayBuffer, intersectionKernelBuffers, materialPropsBuffers, lb,
                        outImageBuffer, scene.getAmbientLightIntensity(), scene.getLightElements().size()
                ));

                blinnPhongKernel.enqueue(getQueue());
            }
        }
    }

    private float[] buildLightMap(Scene scene, int nRays){
        // uniform light intensity map
        float[] lightIntensityMap = new float[scene.getLightElements().size() * nRays];
        int l=0;
        for (LightElement lightElement : scene.getLightElements()) {
            Arrays.fill(lightIntensityMap, l * nRays, (l + 1) * nRays - 1, lightElement.getLight().getIntensity());
            l++;
        }
        return lightIntensityMap;
    }

    @Override
    public void close() {
        super.close();

        viewRaysKernel.close();
        shadowRaysKernel.close();
        blinnPhongKernel.close();
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