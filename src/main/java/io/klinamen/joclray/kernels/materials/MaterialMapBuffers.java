package io.klinamen.joclray.kernels.materials;

import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.cl_context;
import org.jocl.cl_mem;

import java.util.HashMap;

public class MaterialMapBuffers extends KernelBuffersPool {
    private final cl_mem idToQueueIndex;

    private MaterialMapBuffers(cl_mem idToQueueIndex) {
        this.idToQueueIndex = track(idToQueueIndex);
    }

    public cl_mem getIdToQueueIndex() {
        return idToQueueIndex;
    }

    public static MaterialMapBuffers create(cl_context context, Scene scene, HashMap<Integer, Integer> matIndexMap) {
        int[] idToMaterialIndex = scene.getSurfaces().getIntsById(x -> matIndexMap.getOrDefault(x.getId(), 0));
        cl_mem buf = OpenCLUtils.allocateReadOnlyMem(context, idToMaterialIndex);
        return new MaterialMapBuffers(buf);
    }
}
