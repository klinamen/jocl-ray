package io.klinamen.joclray.kernels.shading;

import io.klinamen.joclray.BaseKernelBuffers;
import io.klinamen.joclray.util.OpenCLUtils;
import org.jocl.*;

import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueReadBuffer;

public class ImageBuffer extends BaseKernelBuffers {
    private final cl_mem image;

    private ImageBuffer(cl_mem imageBuffer) {
        this.image = track(imageBuffer);
    }

    public cl_mem getImage() {
        return image;
    }

    public void readTo(cl_command_queue queue, float[] imageBuffer) {
        clEnqueueReadBuffer(queue, image, CL_TRUE, 0,
                Sizeof.cl_float * imageBuffer.length, Pointer.to(imageBuffer), 0, null, null);
    }

    public static ImageBuffer create(cl_context context, float[] imageBuffer) {
        return new ImageBuffer(OpenCLUtils.allocateReadWriteMem(context, imageBuffer));
    }
}
