package io.klinamen.joclray.kernels;

import io.klinamen.joclray.kernels.intersection.IntersectResult;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.scene.LightElement;
import io.klinamen.joclray.util.FloatVec4;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;

public class LightIntensityMapOperation implements OpenCLKernel<LightIntensityMapOperationParams> {
    private final cl_context context;
    private final IntersectionKernelFactory intersectionKernelFactory;
    private LightIntensityMapOperationParams params;

    public LightIntensityMapOperation(cl_context context, IntersectionKernelFactory intersectionKernelFactory) {
        this.context = context;
        this.intersectionKernelFactory = intersectionKernelFactory;
    }

    @Override
    public void setParams(LightIntensityMapOperationParams kernelParams) {
        this.params = kernelParams;
    }

    @Override
    public void enqueue(cl_command_queue queue) {
        final int rays = params.getViewRaysBuffer().getRays();

        RaysGenerationResult shadowRaysResult = new RaysGenerationResult(rays * params.getLights().size());
        RaysBuffers shadowRaysBuffer = RaysBuffers.create(context, shadowRaysResult);

        ShadowRaysKernelParams shadowRaysKernelParams = new ShadowRaysKernelParams(params.getLights(), params.getViewRaysBuffer(), params.getViewRaysIntersectionsBuffers(), shadowRaysBuffer);
        ShadowRaysKernel shadowRaysKernel = new ShadowRaysKernel(context);
        shadowRaysKernel.setParams(shadowRaysKernelParams);

        // compute shadow rays, for each primary ray and light
        shadowRaysKernel.enqueue(queue);

        shadowRaysBuffer.readTo(queue, shadowRaysResult);

        float[] lightIntensityMap = new float[rays * params.getLights().size()];

        int i = 0;
        for (LightElement light : params.getLights()) {
            // TODO try to use FloatBuffer to avoid copying to new buffers
//            FloatBuffer lightShadowRayOrigin = FloatBuffer.wrap(shadowRaysResult.getRayOrigins(), i * rays * FloatVec4.DIM, rays * FloatVec4.DIM);
//            FloatBuffer lightShadowRayDirections = FloatBuffer.wrap(shadowRaysResult.getRayDirections(), i * rays * FloatVec4.DIM, rays * FloatVec4.DIM);
//            RaysBuffers lightShadowRays = RaysBuffers.create(context, lightShadowRayOrigin, lightShadowRayDirections);

            float[] shOrigins = new float[rays * FloatVec4.DIM];
            System.arraycopy(shadowRaysResult.getRayOrigins(), i * shOrigins.length, shOrigins, 0, shOrigins.length);

            float[] shDirs = new float[rays * FloatVec4.DIM];
            System.arraycopy(shadowRaysResult.getRayDirections(), i * shDirs.length, shDirs, 0, shDirs.length);

            RaysGenerationResult shRes = new RaysGenerationResult(shOrigins, shDirs);
            RaysBuffers lightShadowRays = RaysBuffers.create(context, shRes);

            IntersectResult shadowIntersect = new IntersectResult(rays);
            IntersectionKernelBuffers intersectionKernelBuffers = IntersectionKernelBuffers.fromResult(context, shadowIntersect);
            IntersectionOperationParams intersectionOperationParams = new IntersectionOperationParams(params.getSurfaces(), lightShadowRays, intersectionKernelBuffers);
            IntersectionOperation intersectionOperation = new IntersectionOperation(intersectionKernelFactory);
            intersectionOperation.setParams(intersectionOperationParams);

            intersectionOperation.enqueue(queue);

            intersectionKernelBuffers.readTo(queue, shadowIntersect);

            // TODO move to the GPU?
            int[] hitMap = shadowIntersect.getHitMap();
            float[] hitDistances = shadowIntersect.getHitDistances();

            for (int r = 0; r < rays; r++) {
                FloatVec4 shadowOrigin = FloatVec4.extract(shOrigins, r * FloatVec4.DIM);
//                FloatVec4 shadowOrigin = FloatVec4.extract(shadowRaysResult.getRayOrigins(), i * rays * FloatVec4.DIM + r * FloatVec4.DIM);

                float lightDist = shadowOrigin.minus(light.getLight().getPosition()).length();
                float t = hitDistances[r];

                if (hitMap[r] < 0 || t > lightDist) {
                    // lit
                    lightIntensityMap[i * rays + r] = light.getLight().getIntensity();
                } else {
                    // shadow
                    lightIntensityMap[i * rays + r] = 0;
                }

//                if(r > 528848 && r < 528861){
//                    System.out.println(String.format("l:%d, r:%d, hm:%d, ld:%f, t:%f, li:%f", i, r, hitMap[r], lightDist, t, lightIntensityMap[i * rays + r]));
//                }
            }

            i++;
        }

        params.setLightIntensityMap(lightIntensityMap);
    }
}
