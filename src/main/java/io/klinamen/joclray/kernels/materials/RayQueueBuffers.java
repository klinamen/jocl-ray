package io.klinamen.joclray.kernels.materials;

import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.*;

public class RayQueueBuffers extends KernelBuffersPool {
    private final cl_mem queue;
    private final cl_mem queueIndex;

    private final int numQueues;
    private final int queueMaxSize;

    private RayQueueBuffers(int numQueues, int queueMaxSize, cl_mem queue, cl_mem queueIndex) {
        this.numQueues = numQueues;
        this.queueMaxSize = queueMaxSize;

        this.queue = track(queue);
        this.queueIndex = track(queueIndex);
    }

    public cl_mem getQueue() {
        return queue;
    }

    public cl_mem getQueueIndex() {
        return queueIndex;
    }

    public int getNumQueues() {
        return numQueues;
    }

    public int getQueueMaxSize() {
        return queueMaxSize;
    }

    public void readQueueIndex(cl_command_queue queue, int[] buffer) {
        if (buffer.length != numQueues) {
            throw new IllegalArgumentException(String.format("Destination buffer size (%d) does not match device buffer size (%d).", buffer.length, numQueues));
        }

        clEnqueueReadBuffer(queue, queueIndex, CL_TRUE, 0,
                Sizeof.cl_int * buffer.length, Pointer.to(buffer), 0, null, null);
    }

    public void clearQueueIndexBuf(cl_command_queue cmdQueue){
        clEnqueueFillBuffer(cmdQueue, queueIndex, Pointer.to(new int[]{0}), Sizeof.cl_int, 0, numQueues * Sizeof.cl_int, 0, null, null);
    }

    public static RayQueueBuffers create(cl_context context, int nQueues, int queueSize) {
        cl_mem queueIndex = OpenCLUtils.allocateReadWriteMem(context, nQueues, 0);
        cl_mem queue = OpenCLUtils.allocateReadWriteMem(context, nQueues * queueSize, 0);
        return new RayQueueBuffers(nQueues, queueSize, queue, queueIndex);
    }
}
