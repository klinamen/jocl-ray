package io.klinamen.joclray.kernels.tracing;

import org.jocl.cl_context;

public class RaysGen implements AutoCloseable {
    private static int count = 0;

    private final WeightedRaysBuffer buffer;
    private final int generation;
    private final String name;

    public RaysGen(WeightedRaysBuffer buffer, String name) {
        this.buffer = buffer;
        this.generation = 0;
        this.name = name + "_" + count++;
    }

    private RaysGen(WeightedRaysBuffer buffer, int generation, String name) {
        this.buffer = buffer;
        this.generation = generation;
        this.name = name + "_" + count++;
    }

    public WeightedRaysBuffer getBuffer() {
        return buffer;
    }

    public int getGeneration() {
        return generation;
    }

    public String getName() {
        return name;
    }

    public RaysGen derive(WeightedRaysBuffer buffer, String name) {
        return new RaysGen(buffer, generation + 1, String.format("%s,%s", this.name, name));
    }

    public RaysGen derive(cl_context context, String name) {
        return derive(WeightedRaysBuffer.empty(context, buffer.getRaysBuffers().getRays()), name);
    }

    @Override
    public void close() {
        buffer.close();
    }
}
