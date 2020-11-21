package io.klinamen.joclray.kernels.materials;

import io.klinamen.joclray.kernels.AbstractOpenCLKernel;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_context;
import org.jocl.cl_kernel;

import static org.jocl.CL.clSetKernelArg;

public class MaterialSplitKernel extends AbstractOpenCLKernel<MaterialSplitKernelParams> {
    public static final String DEF_N_MATERIALS = "N_MATERIALS";
    public static final String DEF_LOCAL_QUEUE_SIZE = "LOCAL_QUEUE_SIZE";
    public static final String DEF_GLOBAL_QUEUE_SIZE = "GLOBAL_QUEUE_SIZE";

    public static final String KERNEL_NAME = "material_split_global";

    private final int nMaterials;
    private final int localQueueSize;
    private final int globalQueueSize;

    public MaterialSplitKernel(cl_context context, int nMaterials, int localQueueSize, int globalQueueSize) {
        super(context);
        this.nMaterials = nMaterials;
        this.localQueueSize = localQueueSize;
        this.globalQueueSize = globalQueueSize;

        addCompilerOption(OpenCLUtils.defineOption(DEF_N_MATERIALS, nMaterials));
        addCompilerOption(OpenCLUtils.defineOption(DEF_LOCAL_QUEUE_SIZE, localQueueSize));
        addCompilerOption(OpenCLUtils.defineOption(DEF_GLOBAL_QUEUE_SIZE, globalQueueSize));
    }

    @Override
    protected String getKernelName() {
        return KERNEL_NAME;
    }

    @Override
    protected int configureKernel(cl_kernel kernel) {
//        __kernel void material_split(
//                __global const int *hit_map,
//                __global const int *id_to_material,
//                const int queue_size,
//                __global int *ray_queue,
//                __global int *queue_index
//        )

        int a = 0;

        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getIntersectionBuffers().getHitMap()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getMaterialMapBuffers().getIdToQueueIndex()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRayQueueBuffers().getQueue()));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(getParams().getRayQueueBuffers().getQueueIndex()));

        return a;
    }

//    @Override
//    protected long[] getLocalWorkSize() {
//        return new long[]{localQueueSize};
//    }

    @Override
    protected long[] getWorkgroupSize() {
        return new long[]{getParams().getRaysBuffers().getRays()};
    }
}
