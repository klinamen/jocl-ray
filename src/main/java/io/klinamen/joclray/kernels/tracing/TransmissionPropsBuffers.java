package io.klinamen.joclray.kernels.tracing;

import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;
import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.cl_context;
import org.jocl.cl_mem;

public class TransmissionPropsBuffers extends KernelBuffersPool {
    private final cl_mem matKr;
    private final cl_mem matN;

    public TransmissionPropsBuffers(cl_mem matKr, cl_mem matN) {
        this.matKr = track(matKr);
        this.matN = track(matN);
    }

    public cl_mem getMatKr() {
        return matKr;
    }

    public cl_mem getMatN() {
        return matN;
    }

    public static TransmissionPropsBuffers create(cl_context context, Scene scene) {
        ElementSet<SurfaceElement<Surface>> surfaces = scene.getSurfaces();

        cl_mem matKr = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatVec4sById(x -> x.getSurface().getKr()));
        cl_mem matN = OpenCLUtils.allocateReadOnlyMem(context, surfaces.getFloatsById(x -> x.getSurface().getIor()));

        return new TransmissionPropsBuffers(matKr, matN);
    }
}
