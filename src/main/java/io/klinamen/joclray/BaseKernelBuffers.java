package io.klinamen.joclray;

import org.jocl.CL;
import org.jocl.cl_mem;

import java.util.ArrayList;
import java.util.List;

public class BaseKernelBuffers implements AutoCloseable {
    private final List<cl_mem> buffers = new ArrayList<>();

    protected cl_mem track(cl_mem buffer) {
        buffers.add(buffer);
        return buffer;
    }

    protected void releaseTrackedBuffers() {
        buffers.forEach(CL::clReleaseMemObject);
        buffers.clear();
    }

    @Override
    public void close() {
        releaseTrackedBuffers();
    }
}
