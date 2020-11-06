package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueReadBuffer;

public class ImageBuffer extends KernelBuffersPool {
    private final cl_mem image;
    private final int bufferSize;

    private ImageBuffer(cl_mem imageBuffer, int bufferSize) {
        this.image = track(imageBuffer);
        this.bufferSize = bufferSize;
    }

    public cl_mem getImage() {
        return image;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getTotalPixels(){
        return bufferSize / FloatVec4.DIM;
    }

    public void readTo(cl_command_queue queue, float[] imageBuffer) {
        if(imageBuffer.length != bufferSize){
            throw new IllegalArgumentException(String.format("Destination buffer size (%d) does not match image buffer size (%d).", imageBuffer.length, bufferSize));
        }

        clEnqueueReadBuffer(queue, image, CL_TRUE, 0,
                Sizeof.cl_float * imageBuffer.length, Pointer.to(imageBuffer), 0, null, null);
    }

    public static ImageBuffer create(cl_context context, float[] imageBuffer) {
        return new ImageBuffer(OpenCLUtils.allocateReadWriteMem(context, imageBuffer), imageBuffer.length);
    }

    public static ImageBuffer empty(cl_context context, int pixels, float value) {
        return new ImageBuffer(OpenCLUtils.allocateReadWriteMem(context, pixels * FloatVec4.DIM, value), pixels * FloatVec4.DIM);
    }
}
