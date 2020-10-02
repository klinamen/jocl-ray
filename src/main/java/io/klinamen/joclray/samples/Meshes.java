package io.klinamen.joclray.samples;

import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.loaders.ObjSurfaceLoader;
import io.klinamen.joclray.loaders.SurfaceLoader;

import java.io.IOException;
import java.io.InputStream;

public class Meshes {
    private static final SurfaceLoader loader = new ObjSurfaceLoader();

    public static TriangleMesh teapot() {
        return load("/meshes/teapot.obj");
    }

    public static TriangleMesh teapotLow() {
        return load("/meshes/teapot-low.obj");
    }

    public static TriangleMesh bunnyLow() {
        return load("/meshes/bunny-low.obj");
    }

    public static TriangleMesh bunny() {
        return load("/meshes/bunny.obj");
    }

    public static TriangleMesh cube() {
        return load("/meshes/cube.obj");
    }

    private static TriangleMesh load(String resourceName) {
        try {
            InputStream inputStream = Meshes.class.getResourceAsStream(resourceName);
            if(inputStream == null){
                throw new RuntimeException(String.format("Unable to find resource for '%s'.", resourceName));
            }

            return (TriangleMesh)loader.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error trying to load surface for resource '%s': %s", resourceName, e.getMessage()), e);
        }
    }
}
