package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.light.SpotLight;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.LightElement;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import java.util.Arrays;

import static org.jocl.CL.*;

public class ShadingKernelBuffers implements AutoCloseable {
    private final cl_mem krPrev;

    private final cl_mem kd;
    private final cl_mem ks;
    private final cl_mem kr;
    private final cl_mem phongExp;

    private final cl_mem lightPos;
    private final cl_mem lightIntensity;
    private final cl_mem lightDirection;
    private final cl_mem lightAngleRad;

    private final cl_mem image;

    private ShadingKernelBuffers(cl_mem krPrev, cl_mem kd, cl_mem ks, cl_mem kr, cl_mem phongExp, cl_mem lightPos, cl_mem lightIntensity, cl_mem lightDirection, cl_mem lightAngleRad, cl_mem image) {
        this.krPrev = krPrev;
        this.kd = kd;
        this.ks = ks;
        this.kr = kr;
        this.phongExp = phongExp;
        this.lightPos = lightPos;
        this.lightIntensity = lightIntensity;
        this.lightDirection = lightDirection;
        this.lightAngleRad = lightAngleRad;
        this.image = image;
    }

    public cl_mem getKd() {
        return kd;
    }

    public cl_mem getKs() {
        return ks;
    }

    public cl_mem getKr() {
        return kr;
    }

    public cl_mem getPhongExp() {
        return phongExp;
    }

    public cl_mem getLightPos() {
        return lightPos;
    }

    public cl_mem getLightIntensity() {
        return lightIntensity;
    }

    public cl_mem getImage() {
        return image;
    }

    public cl_mem getKrPrev() {
        return krPrev;
    }

    public cl_mem getLightDirection() {
        return lightDirection;
    }

    public cl_mem getLightAngleRad() {
        return lightAngleRad;
    }

    public static ShadingKernelBuffers create(cl_context context, int rays, Scene scene, float[] lightIntensityMap, float[] imageBuffer) {
        cl_mem image = OpenCLUtils.allocateReadWriteMem(context, imageBuffer);

        // full contribution for primary rays -> initialized to 1
        float[] krPrevBuf = new float[rays * FloatVec4.DIM];
        Arrays.fill(krPrevBuf, 1f);
        cl_mem krPrev = OpenCLUtils.allocateReadWriteMem(context, krPrevBuf);

        ElementSet<SurfaceElement<Surface>> surfaces = scene.getSurfaces();
        cl_mem kd = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getKd()));
        cl_mem ks = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getKs()));
        cl_mem kr = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getKr()));
        cl_mem phongExp = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatsById(x -> x.getSurface().getPhongExp()));

        ElementSet<LightElement> lights = scene.getLightElements();
        cl_mem lightPos = OpenCLUtils.allocateReadOnlyMem(context, lights.getFloatVec4s(x -> x.getLight().getPosition()));
        cl_mem lightIntensity = OpenCLUtils.allocateReadOnlyMem(context, lightIntensityMap);

        // TODO hacky
        cl_mem lightDirection = OpenCLUtils.allocateReadOnlyMem(context, lights.getFloatVec4s(x -> {
            PointLight light = x.getLight();
            if(light instanceof SpotLight){
                return ((SpotLight) light).getDirection();
            }

            return new FloatVec4();
        }));

        // TODO hacky
        cl_mem lightAngleRad = OpenCLUtils.allocateReadOnlyMem(context, lights.getFloats(x -> {
            PointLight light = x.getLight();
            if(light instanceof SpotLight){
                return ((SpotLight) light).getAngleRad();
            }

            return 0f;
        }));

        return new ShadingKernelBuffers(
                krPrev,
                kd,
                ks,
                kr,
                phongExp,
                lightPos,
                lightIntensity,
                lightDirection, lightAngleRad, image
        );
    }

    public void readTo(cl_command_queue queue, float[] imageBuffer) {
        clEnqueueReadBuffer(queue, image, CL_TRUE, 0,
                Sizeof.cl_float * imageBuffer.length, Pointer.to(imageBuffer), 0, null, null);
    }

    @Override
    public void close() {
        clReleaseMemObject(kd);
        clReleaseMemObject(ks);
        clReleaseMemObject(kr);
        clReleaseMemObject(phongExp);
        clReleaseMemObject(lightPos);
        clReleaseMemObject(lightIntensity);
        clReleaseMemObject(image);
        clReleaseMemObject(lightDirection);
        clReleaseMemObject(lightAngleRad);
    }
}
