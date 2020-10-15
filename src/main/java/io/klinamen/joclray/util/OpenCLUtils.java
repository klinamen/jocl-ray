package io.klinamen.joclray.util;

import org.jocl.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
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
        String resourceName = "/kernels/" + kernelName + ".cl";
        try (InputStream inputStream = OpenCLUtils.class.getResourceAsStream(resourceName)) {
            if(inputStream == null){
                throw new RuntimeException(String.format("Unable ti find kernel source for resource '%s'.", resourceName));
            }

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

    public static cl_mem allocateReadWriteMem(cl_context context, int size, float value) {
        float[] weights = new float[size];
        Arrays.fill(weights, value);
        return OpenCLUtils.allocateReadWriteMem(context, weights);
    }

    public static cl_mem allocateReadWriteMem(cl_context context, int size, int value) {
        int[] values = new int[size];
        Arrays.fill(values, value);
        return OpenCLUtils.allocateReadWriteMem(context, values);
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

    public static String defineOption(String name, String value){
        return String.format("-D %s=%s", name, value);
    }
}
