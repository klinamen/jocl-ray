package io.klinamen.joclray;

import org.jocl.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Scanner;

import static org.jocl.CL.*;

public class OpenCLUtils {
    public static cl_program getProgramForKernel(cl_context context, String kernelName) {
        try {
            String source = getKernelSource(kernelName);
            return clCreateProgramWithSource(context,
                    1, new String[]{source}, null, null);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to load kernel %s: %s", kernelName, e.getMessage()), e);
        }
    }

    public static String getKernelSource(String kernelName) throws IOException {
        try (InputStream inputStream = OpenCLUtils.class.getResourceAsStream("/kernels/" + kernelName + ".cl")) {
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            return result;
        }
    }

    public static cl_mem allocateReadWriteMem(cl_context context, int[] hostBuffer) {
        return clCreateBuffer(context,
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * hostBuffer.length, Pointer.to(hostBuffer), null);
    }

    public static cl_mem allocateReadWriteMem(cl_context context, float[] hostBuffer) {
        return clCreateBuffer(context,
                CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * hostBuffer.length, Pointer.to(hostBuffer), null);
    }

    public static cl_mem allocateReadOnlyMem(cl_context context, FloatBuffer hostBuffer) {
        return clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * (hostBuffer.limit() - hostBuffer.position()), Pointer.toBuffer(hostBuffer), null);
    }

    public static cl_mem allocateReadOnlyMem(cl_context context, int[] hostBuffer) {
        return clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * hostBuffer.length, Pointer.to(hostBuffer), null);
    }

    public static cl_mem allocateReadOnlyMem(cl_context context, float[] hostBuffer) {
        return clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * hostBuffer.length, Pointer.to(hostBuffer), null);
    }
}
