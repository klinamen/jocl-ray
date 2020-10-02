package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.kernels.OpenCLOperation;
import org.jocl.cl_command_queue;

public class ShadingOperation implements OpenCLOperation {
    private final OpenCLOperation intersection;
    private final OpenCLOperation shading;
    private final int bounces;

    public ShadingOperation(OpenCLOperation intersection, OpenCLOperation shading, int bounces) {
        this.intersection = intersection;
        this.shading = shading;
        this.bounces = bounces;
    }

    @Override
    public void enqueue(cl_command_queue queue) {
        for (int i = 0; i < bounces + 1; i++) {
            if(i > 0) {
                // skip first intersection
                intersection.enqueue(queue);
            }
            shading.enqueue(queue);
        }
    }
}
