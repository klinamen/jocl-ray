package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.util.KernelBuffersPool;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueReadBuffer;

public class ImageBuffer extends KernelBuffersPool {
    private final cl_mem image;
    private final int size;

    private ImageBuffer(cl_mem imageBuffer, int size) {
        this.image = track(imageBuffer);
        this.size = size;
    }

    public cl_mem getImage() {
        return image;
    }

    public int getSize() {
        return size;
    }

    public void readTo(cl_command_queue queue, float[] imageBuffer) {
        if(imageBuffer.length != size){
            throw new IllegalArgumentException(String.format("Destination buffer size (%d) does nto match image buffer size (%d).", imageBuffer.length, size));
        }

        clEnqueueReadBuffer(queue, image, CL_TRUE, 0,
                Sizeof.cl_float * imageBuffer.length, Pointer.to(imageBuffer), 0, null, null);
    }

    public static ImageBuffer create(cl_context context, float[] imageBuffer) {
        return new ImageBuffer(OpenCLUtils.allocateReadWriteMem(context, imageBuffer), imageBuffer.length);
    }
}
