package io.klinamen.joclray.kernels;

import org.jocl.cl_command_queue;

public abstract class AbstractOpenCLOperation implements OpenCLOperation {
    @Override
    public void enqueue(cl_command_queue queue) {
//        System.out.println("Enqueue " + this.getClass().getSimpleName());
        doEnqueue(queue);
    }

    protected abstract void doEnqueue(cl_command_queue queue);
}
