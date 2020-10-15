package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.BaseKernelBuffers;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.light.SpotLight;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.LightElement;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.cl_context;
import org.jocl.cl_mem;

public class LightingBuffers extends BaseKernelBuffers {
    private final cl_mem lightPos;
    private final cl_mem lightIntensity;
    private final cl_mem lightDirection;
    private final cl_mem lightAngleRad;
    private final cl_mem lightFallout;

    private LightingBuffers(cl_mem lightPos, cl_mem lightIntensity, cl_mem lightDirection, cl_mem lightAngleRad, cl_mem lightFallout) {
        this.lightPos = track(lightPos);
        this.lightIntensity = track(lightIntensity);
        this.lightDirection = track(lightDirection);
        this.lightAngleRad = track(lightAngleRad);
        this.lightFallout = track(lightFallout);
    }

    public cl_mem getLightPos() {
        return lightPos;
    }

    public cl_mem getLightIntensity() {
        return lightIntensity;
    }

    public cl_mem getLightDirection() {
        return lightDirection;
    }

    public cl_mem getLightAngleRad() {
        return lightAngleRad;
    }

    public cl_mem getLightFallout() {
        return lightFallout;
    }

    public static LightingBuffers create(cl_context context, Scene scene, float[] lightIntensityMap) {
        ElementSet<LightElement> lights = scene.getLightElements();
        cl_mem lightPos = OpenCLUtils.allocateReadOnlyMem(context, lights.getFloatVec4s(x -> x.getLight().getPosition()));
        cl_mem lightIntensity = OpenCLUtils.allocateReadOnlyMem(context, lightIntensityMap);

        // TODO hacky
        cl_mem lightDirection = OpenCLUtils.allocateReadOnlyMem(context, lights.getFloatVec4s(x -> {
            PointLight light = x.getLight();
            if (light instanceof SpotLight) {
                return ((SpotLight) light).getDirection();
            }

            return new FloatVec4();
        }));

        // TODO hacky
        cl_mem lightAngleRad = OpenCLUtils.allocateReadOnlyMem(context, lights.getFloats(x -> {
            PointLight light = x.getLight();
            if (light instanceof SpotLight) {
                return ((SpotLight) light).getAngleRad();
            }

            return 0f;
        }));

        // TODO hacky
        cl_mem lightFallout = OpenCLUtils.allocateReadOnlyMem(context, lights.getFloats(x -> {
            PointLight light = x.getLight();
            if (light instanceof SpotLight) {
                return ((SpotLight) light).getFallout();
            }

            return 0f;
        }));

        return new LightingBuffers(lightPos, lightIntensity, lightDirection, lightAngleRad, lightFallout);
    }
}
