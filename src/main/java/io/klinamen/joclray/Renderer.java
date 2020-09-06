package io.klinamen.joclray;

import io.klinamen.joclray.display.IntersectionsDisplay;
import io.klinamen.joclray.display.ShadingDisplay;
import io.klinamen.joclray.kernels.*;
import io.klinamen.joclray.kernels.intersection.IntersectResult;
import io.klinamen.joclray.kernels.intersection.IntersectionKernelBuffers;
import io.klinamen.joclray.kernels.intersection.IntersectionOperation;
import io.klinamen.joclray.kernels.intersection.IntersectionOperationParams;
import io.klinamen.joclray.kernels.intersection.factory.IntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.PrototypeIntersectionKernelFactory;
import io.klinamen.joclray.kernels.intersection.factory.RegistryIntersectionKernelFactory;
import io.klinamen.joclray.kernels.shading.ShadingKernel;
import io.klinamen.joclray.kernels.shading.ShadingKernelBuffers;
import io.klinamen.joclray.kernels.shading.ShadingKernelParams;
import io.klinamen.joclray.kernels.shading.ShadingOperation;
import io.klinamen.joclray.scene.Scene;
import org.jocl.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static org.jocl.CL.*;

public class Renderer implements AutoCloseable {
    private cl_context context;
    private cl_command_queue queue;

    public Renderer() {
        initCL();
    }

    private int getPixels(Scene scene) {
        return (int) scene.getCamera().getFrameWidth() * (int) scene.getCamera().getFrameHeight();
    }

    public void cast(Scene scene, BufferedImage outImage) {
        long startTime = System.nanoTime();

        int nPixels = getPixels(scene);

        RaysGenerationResult rays = new RaysGenerationResult(nPixels);
        RaysBuffers raysBuffers = RaysBuffers.create(context, rays);
        ViewRaysKernelParams viewRaysKernelParams = new ViewRaysKernelParams(outImage.getWidth(), outImage.getHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), raysBuffers);
        ViewRaysKernel viewRaysKernel = new ViewRaysKernel(context);
        viewRaysKernel.setParams(viewRaysKernelParams);

        IntersectResult intersectResult = new IntersectResult(nPixels);
        RegistryIntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(context);
        IntersectionKernelBuffers intersectionKernelBuffers = IntersectionKernelBuffers.fromResult(context, intersectResult);
        IntersectionOperationParams intersectionOperationParams = new IntersectionOperationParams(scene.getSurfaces(), raysBuffers, intersectionKernelBuffers);
        IntersectionOperation intersectionOperation = new IntersectionOperation(intersectionKernelFactory);
        intersectionOperation.setParams(intersectionOperationParams);

        // cast
        viewRaysKernel.enqueue(queue);
        intersectionOperation.enqueue(queue);

        intersectionKernelBuffers.readTo(queue, intersectResult);

        long elapsed = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + elapsed/1000000 + " ms");

        // update image
        new IntersectionsDisplay(scene, intersectResult).update(outImage);
    }

    public void render(Scene scene, BufferedImage outImage) {
        long startTime = System.nanoTime();

        int nPixels = getPixels(scene);
        float[] imageBuffer = new float[nPixels * FloatVec4.DIM];

        IntersectionKernelFactory intersectionKernelFactory = new PrototypeIntersectionKernelFactory(context);
//        IntersectionKernelFactory intersectionKernelFactory = new RegistryIntersectionKernelFactory(context);

        RaysGenerationResult rays = new RaysGenerationResult(nPixels);
        RaysBuffers viewRaysBuffers = RaysBuffers.create(context, rays);
        ViewRaysKernelParams viewRaysKernelParams = new ViewRaysKernelParams(outImage.getWidth(), outImage.getHeight(), scene.getOrigin(), scene.getCamera().getFovRad(), viewRaysBuffers);
        ViewRaysKernel viewRaysKernel = new ViewRaysKernel(context);
        viewRaysKernel.setParams(viewRaysKernelParams);

        // generate view rays
        viewRaysKernel.enqueue(queue);

        IntersectResult intersectResult = new IntersectResult(nPixels);
        IntersectionKernelBuffers viewRaysIntersectionsBuffers = IntersectionKernelBuffers.fromResult(context, intersectResult);
        IntersectionOperationParams viewRayIntersectionParams = new IntersectionOperationParams(scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers);
        IntersectionOperation viewRaysIntersection = new IntersectionOperation(intersectionKernelFactory);
        viewRaysIntersection.setParams(viewRayIntersectionParams);

        // primary ray intersections
        viewRaysIntersection.enqueue(queue);

        LightIntensityMapOperationParams lightIntensityMapOperationParams = new LightIntensityMapOperationParams(scene.getLightElements(), scene.getSurfaces(), viewRaysBuffers, viewRaysIntersectionsBuffers);
        LightIntensityMapOperation lightIntensityMapOperation = new LightIntensityMapOperation(context, intersectionKernelFactory);
        lightIntensityMapOperation.setParams(lightIntensityMapOperationParams);

        // compute light intensity map (for shadows)
        lightIntensityMapOperation.enqueue(queue);

        float[] lightIntensityMap = lightIntensityMapOperationParams.getLightIntensityMap();

        ShadingKernelBuffers shadingKernelBuffers = ShadingKernelBuffers.create(context, viewRaysBuffers, viewRaysIntersectionsBuffers, scene, lightIntensityMap, imageBuffer);
        ShadingKernelParams shadingKernelParams = new ShadingKernelParams(scene.getAmbientLightIntensity(), scene.getLightElements().size(), shadingKernelBuffers);
        ShadingKernel shadingKernel = new ShadingKernel(context);
        shadingKernel.setParams(shadingKernelParams);

        ShadingOperation shadingOperation = new ShadingOperation(viewRaysIntersection, shadingKernel, 4);

        // shading
        shadingOperation.enqueue(queue);

        shadingKernelBuffers.readTo(queue, imageBuffer);

        long elapsed = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + elapsed/1000000 + " ms");

        // update image
        new ShadingDisplay(scene, imageBuffer).update(outImage);
    }

    private void initCL() {
        // The platform, device type and device number
        // that will be used
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int platformIndex = 1;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        cl_platform_id[] platforms = getPlatforms();
        Map<Integer, cl_device_id[]> devicesByPlatform = new HashMap<>();

        for (int i = 0; i < platforms.length; i++) {
            cl_platform_id platform = platforms[i];
            cl_device_id[] platformDevices = getDevices(platform, deviceType);
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
                System.out.println("  " + (platformIndex==key && deviceIndex==i ? "*" : " ") + "  " + i + ": " + getDeviceDesc(devs[i]));
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

    private String getPlatformDesc(cl_platform_id platform){
        String vendorName = getString(platform, CL_PLATFORM_VENDOR);
        String deviceName = getString(platform, CL_PLATFORM_NAME);
        return String.format("%s (%s)", deviceName, vendorName);
    }

    private String getDeviceDesc(cl_device_id device){
        String vendorName = getString(device, CL_DEVICE_VENDOR);
        String deviceName = getString(device, CL_DEVICE_NAME);
        return String.format("%s (%s)", deviceName, vendorName);
    }

    private cl_platform_id[] getPlatforms(){
        // Obtain the number of platforms
        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);

        return platforms;
    }

    private cl_device_id[] getDevices(cl_platform_id platform, long deviceType){
        // Obtain the number of devices for the platform
        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);

        return devices;
    }

    @Override
    public void close() {
        clReleaseCommandQueue(queue);
        clReleaseContext(context);
    }

    /**
     * Returns the value of the platform info parameter with the given name
     *
     * @param platform The platform
     * @param paramName The parameter name
     * @return The value
     */
    private String getString(cl_platform_id platform, int paramName)
    {
        long[] size = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);
        byte[] buffer = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName,
                buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length-1);
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private String getString(cl_device_id device, int paramName)
    {
        long[] size = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);
        byte[] buffer = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName,
                buffer.length, Pointer.to(buffer), null);
        return new String(buffer, 0, buffer.length-1);
    }
}
