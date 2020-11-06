package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.cl_context;
import org.jocl.cl_mem;

public class DiffusePropsBuffers extends KernelBuffersPool {
    private final cl_mem matKd;
    private final cl_mem matEmission;

    public DiffusePropsBuffers(cl_mem matKd, cl_mem matEmission) {
        this.matKd = track(matKd);
        this.matEmission = track(matEmission);
    }

    public cl_mem getMatKd() {
        return matKd;
    }

    public cl_mem getMatEmission() {
        return matEmission;
    }

    public static DiffusePropsBuffers create(cl_context context, Scene scene) {
        ElementSet<SurfaceElement<Surface>> surfaces = scene.getSurfaces();

        cl_mem matKr = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getKd()));
        cl_mem matN = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getEmission()));

        return new DiffusePropsBuffers(matKr, matN);
    }
}
