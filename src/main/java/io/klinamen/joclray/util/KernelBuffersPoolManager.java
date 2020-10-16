package io.klinamen.joclray.util;

import org.jocl.*;

import java.util.ArrayList;
import java.util.List;

import static org.jocl.CL.*;

public class KernelBuffersPoolManager implements AutoCloseable {
    private final String name;
    private final List<cl_mem> buffers = new ArrayList<>();

    public KernelBuffersPoolManager(String name) {
        this.name = name;
    }

    public cl_mem track(cl_mem buffer) {
        buffers.add(buffer);
//        System.out.println(name + ".track: " + bufferInfo(buffer));
        return buffer;
    }

    private void releaseBuffer(cl_mem buffer) {
//        System.out.println(name + ".release: " + bufferInfo(buffer));
        clReleaseMemObject(buffer);
    }

    @Override
    public void close() {
        buffers.forEach(this::releaseBuffer);
        buffers.clear();
    }

    private String bufferInfo(cl_mem buffer) {
        int[] size = new int[1];
        CL.clGetMemObjectInfo(buffer, CL.CL_MEM_SIZE, Sizeof.size_t, Pointer.to(size), null);

        long[] refCount = new long[1];
        CL.clGetMemObjectInfo(buffer, CL.CL_MEM_REFERENCE_COUNT, Sizeof.cl_long, Pointer.to(refCount), null);

        return String.format("size=%d, rc=%d", size[0], refCount[0]);
    }

    public void migrateToHost(cl_command_queue queue, cl_event event){
        clEnqueueMigrateMemObjects(queue, buffers.size(), buffers.toArray(new cl_mem[0]), CL_MIGRATE_MEM_OBJECT_HOST, 0, null, event);
    }

    public void migrateToHost(cl_command_queue queue){
        migrateToHost(queue, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + "(");
        for (int i = 0; i < buffers.size(); i++) {
            cl_mem buffer = buffers.get(i);
            sb.append(String.format("%d: %s;", i, bufferInfo(buffer)));
        }
        sb.append(")");
        return sb.toString();
    }
}
