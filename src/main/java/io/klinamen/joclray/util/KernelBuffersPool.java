package io.klinamen.joclray.util;

import org.jocl.cl_command_queue;
import org.jocl.cl_event;
import org.jocl.cl_mem;

public class KernelBuffersPool implements AutoCloseable {
    private final KernelBuffersPoolManager buffersPoolManager = new KernelBuffersPoolManager(this.getClass().getSimpleName());

    protected cl_mem track(cl_mem buffer) {
        return buffersPoolManager.track(buffer);
    }

    @Override
    public void close() {
        buffersPoolManager.close();
    }

    public void migrateToHost(cl_command_queue queue, cl_event event){
        buffersPoolManager.migrateToHost(queue, event);
    }

    public void migrateToHost(cl_command_queue queue){
        buffersPoolManager.migrateToHost(queue);
    }

    @Override
    public String toString() {
        return buffersPoolManager.toString();
    }
}
