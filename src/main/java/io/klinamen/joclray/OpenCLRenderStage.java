package io.klinamen.joclray;

import org.jocl.*;

import static org.jocl.CL.*;

public abstract class OpenCLRenderStage {
    private final cl_context context;
    private final cl_command_queue queue;

    public OpenCLRenderStage(cl_context context, cl_command_queue queue) {
        this.context = context;
        this.queue = queue;
    }

    protected cl_context getContext() {
        return context;
    }

    protected cl_command_queue getQueue() {
        return queue;
    }

    protected cl_mem createInputBuf(int[] hostBuffer) {
        return clCreateBuffer(getContext(),
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * hostBuffer.length, Pointer.to(hostBuffer), null);
    }

    protected cl_mem createInputBuf(float[] hostBuffer) {
        return clCreateBuffer(getContext(),
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * hostBuffer.length, Pointer.to(hostBuffer), null);
    }
}
