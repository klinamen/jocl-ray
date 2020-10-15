package io.klinamen.joclray.kernels.shading.blinnphong;

import io.klinamen.joclray.BaseKernelBuffers;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.cl_context;
import org.jocl.cl_mem;

public class BlinnPhongMaterialPropsBuffers extends BaseKernelBuffers {
    private final cl_mem kd;
    private final cl_mem ks;
    private final cl_mem phongExp;

    private BlinnPhongMaterialPropsBuffers(cl_mem kd, cl_mem ks, cl_mem phongExp) {
        this.kd = track(kd);
        this.ks = track(ks);
        this.phongExp = track(phongExp);
    }

    public cl_mem getKd() {
        return kd;
    }

    public cl_mem getKs() {
        return ks;
    }

    public cl_mem getPhongExp() {
        return phongExp;
    }

    public static BlinnPhongMaterialPropsBuffers create(cl_context context, Scene scene) {
        ElementSet<SurfaceElement<Surface>> surfaces = scene.getSurfaces();
        cl_mem kd = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getKd()));
        cl_mem ks = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getKs()));
        cl_mem phongExp = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatsById(x -> x.getSurface().getPhongExp()));

        return new BlinnPhongMaterialPropsBuffers(kd, ks, phongExp);
    }
}
