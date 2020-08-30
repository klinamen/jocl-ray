package io.klinamen.joclray;

import org.jocl.cl_context;
import org.jocl.cl_program;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.jocl.CL.clCreateProgramWithSource;

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
}
