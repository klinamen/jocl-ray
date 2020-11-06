package io.klinamen.joclray.rendering;

import io.klinamen.joclray.scene.Scene;
import org.jocl.*;

import java.util.HashMap;
import java.util.Map;

import static org.jocl.CL.*;

public abstract class AbstractOpenCLRenderer implements Renderer, AutoCloseable {
    private cl_context context;
    private cl_command_queue queue;

    private int platformIndex = 0;
    private int deviceIndex = 0;

    public AbstractOpenCLRenderer() {
    }

    public AbstractOpenCLRenderer(int platformIndex, int deviceIndex) {
        this.platformIndex = platformIndex;
        this.deviceIndex = deviceIndex;
    }

    protected cl_context getContext() {
        if(context == null){
            initCL();
        }

        return context;
    }

    protected cl_command_queue getQueue() {
        if(queue == null){
            initCL();
        }

        return queue;
    }

    private void initCL() {
        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        cl_platform_id[] platforms = getPlatforms();
        Map<Integer, cl_device_id[]> devicesByPlatform = new HashMap<>();

        for (int i = 0; i < platforms.length; i++) {
            cl_platform_id platform = platforms[i];
            cl_device_id[] platformDevices = getDevices(platform, CL_DEVICE_TYPE_ALL);
            for (int j = 0; j < platformDevices.length; j++) {
                cl_device_id[] devs = devicesByPlatform.getOrDefault(i, new cl_device_id[platformDevices.length]);
                devs[j] = platformDevices[j];
                devicesByPlatform.putIfAbsent(i, devs);
            }
        }

        System.out.println("--- Available platforms and devices ---");
        for (Integer key : devicesByPlatform.keySet()) {
            System.out.println(key + ": " + getPlatformDesc(platforms[key]));
            cl_device_id[] devs = devicesByPlatform.get(key);
            for (int i = 0; i < devs.length; i++) {
                System.out.println("  " + (platformIndex == key && deviceIndex == i ? "*" : " ") + "  " + i + ": " + getDeviceDesc(devs[i]));
            }
        }
        System.out.println("---");

        cl_platform_id platform = platforms[platformIndex];

        cl_device_id[] platformDevices = devicesByPlatform.get(platformIndex);
        cl_device_id device = platformDevices[deviceIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Create a context for the selected device
        context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        cl_queue_properties properties = new cl_queue_properties();
        queue = clCreateCommandQueueWithProperties(
                context, device, properties, null);
    }

    private String getPlatformDesc(cl_platform_id platform) {
        String vendorName = getString(platform, CL_PLATFORM_VENDOR);
        String deviceName = getString(platform, CL_PLATFORM_NAME);
        return String.format("%s (%s)", deviceName, vendorName);
    }

    private String getDeviceDesc(cl_device_id device) {
        String vendorName = getString(device, CL_DEVICE_VENDOR);
        String deviceName = getString(device, CL_DEVICE_NAME);
        return String.format("%s (%s)", deviceName, vendorName);
    }

    private cl_platform_id[] getPlatforms() {
        // Obtain the number of platforms
        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);

        return platforms;
    }

    private cl_device_id[] getDevices(cl_platform_id platform, long deviceType) {
        // Obtain the number of devices for the platform
        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);

        return devices;
    }

    /**
     * Returns the value of the platform info parameter with the given name
     *
     * @param platform  The platform
     * @param paramName The parameter name
     * @return The value
     */
    private String getString(cl_platform_id platform, int paramName) {
        long[] size = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);
        byte[] buffer = new byte[(int) size[0]];
        clGetPlatformInfo(platform, paramName,
                buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length - 1);
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device    The device
     * @param paramName The parameter name
     * @return The value
     */
    private String getString(cl_device_id device, int paramName) {
        long[] size = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);
        byte[] buffer = new byte[(int) size[0]];
        clGetDeviceInfo(device, paramName,
                buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length - 1);
    }

    @Override
    public float[] render(Scene scene) {
        long startTime = System.nanoTime();

        float[] outBuffer = doRender(scene);

        long elapsed = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + elapsed/1000000 + " ms");

        return outBuffer;
    }

    protected abstract float[] doRender(Scene scene);

    @Override
    public void close() {
        if(queue != null) {
            clReleaseCommandQueue(queue);
        }

        if(context != null) {
            clReleaseContext(context);
        }
    }
}
