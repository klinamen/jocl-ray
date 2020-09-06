package io.klinamen.joclray.kernels;

import org.jocl.cl_command_queue;

public interface OpenCLOperation {
    void enqueue(cl_command_queue queue);
}
