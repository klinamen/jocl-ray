package io.klinamen.joclray.samples;

import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.materials.AshikhminShirley;
import io.klinamen.joclray.materials.Lambertian;
import io.klinamen.joclray.materials.ProceduralWood;
import io.klinamen.joclray.scene.Camera;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.transformations.ejml.*;
import io.klinamen.joclray.util.FloatVec4;

public class Scene6_S3 {
    public static Scene build() {
        Scene scene = new Scene(new Camera()
                .setFovGrad(50)
        )
                .setAmbientLightIntensity(0.2f)
                .add("Ceiling", new Plane()
                        .setNormal(new FloatVec4(0, -1, 0))
                        .setPosition(new FloatVec4(0, 12, 0))
                        .setMaterial(new Lambertian()
                                .setKd(new FloatVec4(0.752f, 0.901f, 0.925f))
                        )
                )
                .add("Floor", new Plane()
                        .setNormal(new FloatVec4(0, 1, 0))
                        .setPosition(new FloatVec4(0, -5, 0))
                        .setMaterial(new AshikhminShirley()
                                .setKd(new FloatVec4(0.9f, 0.6f, 0.8f))
                                .setKr(new FloatVec4(0.8f, 0.8f, 0.8f))
                                .setNu(10)
                                .setNv(10000)
                        )
                )
                .add("Back_Wall", new Plane()
                        .setNormal(new FloatVec4(0, 0, 1))
                        .setPosition(new FloatVec4(0, 0, -40))
                        .setMaterial(new Lambertian()
                                .setKd(new FloatVec4(0.752f, 0.901f, 0.925f))

                        )
                )
                .add("Left_Wall", new Plane()
                        .setNormal(new FloatVec4(1, 0, 0))
                        .setPosition(new FloatVec4(-10, 0, 0))
                        .setMaterial(new ProceduralWood()
                                .setKd(new FloatVec4(0.4f, 0.7f, 1f))
                        )
                )
                .add("Right_Wall", new Plane()
                        .setNormal(new FloatVec4(-1, 0, 0))
                        .setPosition(new FloatVec4(10, 0, 0))
                        .setMaterial(new Lambertian()
                                .setKd(new FloatVec4(0.8f, 0.4f, 0.3f))
                        )
                )
                .add("Front_Wall", new Plane()
                        .setNormal(new FloatVec4(0, 0, 1))
                        .setPosition(new FloatVec4(0, 0, 10))
                        .setMaterial(new Lambertian()
                                .setKd(new FloatVec4(0.752f, 0.901f, 0.925f))
                        )
                )
                .add("Pearl", new Sphere()
                        .setCenter(new FloatVec4(-6, -1f, -25))
                        .setRadius(4f)
                        .setMaterial(new AshikhminShirley()
                                .setKd(new FloatVec4(0.6f, 0.7f, 0.4f))
                                .setKr(new FloatVec4(0.2f, 0.2f, 0.2f))
                                .setNu(10)
                                .setNv(100000)
                        )
                )
                .add("Sphere_Diffuse", new Sphere()
                        .setCenter(new FloatVec4(0, -3.5f, -18))
                        .setRadius(1.5f)
                        .setMaterial(new Lambertian()
                                .setKd(new FloatVec4(0.2f, 0.8f, 0.8f))
                        )
                )
                .add("Cube_Blue", Meshes.cube()
                        .transform(new CompositeTransformation()
                                .add(RotateYTransformation.withGradAngle(60))
                                .add(new ScaleTransformation(5, 7, 4))
                                .add(new TranslateTransformation(4, -2, -20))
                        )
                        .setMaterial(new Lambertian()
                                .setKd(new FloatVec4(1.2f, 1.2f, 1.2f))
                        )
                )
//                .add("Teapot", teapot()
//                        .transform(new TranslateTransformation(4, -5, -20))
//                        .setKd(new FloatVec4(0.3f, 0.3f, 0.3f))
//                        .setKr(new FloatVec4(0.2f, 0.2f, 0.1f))
//                )
                ;

        addSphereLight(scene, "Ceiling", new FloatVec4(0, 14f, -22f), 4.5f, new FloatVec4(100, 100, 100), 1.5f);
//        addSphereLight(scene, "Front", new FloatVec4(-6, 8, 0), 4, new FloatVec4(10, 10, 10), 1.5f);
//        addXZLightsArray(scene, 30, 30, 4, 4, new FloatVec4(-15f, 15.8f, -45f));

//          addXZLightsArray(scene, 16, 16, 4, 4, new FloatVec4(-8f, 11.5f, -35));

        return scene;
    }

    private static void addSphereLight(Scene scene, String nameSuffix, FloatVec4 position, float radius, FloatVec4 emission, float lightIntensity) {
        scene.add("SphereLight_" + nameSuffix, new Sphere()
                .setCenter(position)
                .setRadius(radius)
                .setEmission(emission)
        );

        scene.add("PointLight_" + nameSuffix, new PointLight()
                .setPosition(position)
                .setIntensity(lightIntensity)
        );
    }

    private static void addXZLightsArray(Scene scene, float xSize, float zSize, int xCount, int zCount, FloatVec4 corner) {
        final float xSpacing = xSize / (xCount - 1);
        final float zSpacing = zSize / (zCount - 1);
        final float r = Math.min(xSpacing, zSpacing) / 10;
        FloatVec4 emission = new FloatVec4(1000, 1000, 1000).div(xCount * zCount);

        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < zCount; j++) {
                FloatVec4 pos = corner.plus(new FloatVec4(i * xSpacing, 0, j * xSpacing));
                FloatVec4 mod = new FloatVec4(i % 2 == 0 ? 1.5f : 1, j % 2 == 0 ? 1.5f : 1f, (i + j) % 2 == 0 ? 1.5f : 1f);
                addSphereLight(scene, String.format("%d_%d", i, j), pos, r, emission.mul(mod), 3f / (xCount * zCount));
            }
        }
    }

    private static TriangleMesh teapot() {
        return (TriangleMesh) Meshes.teapotLow()
                .transform(new CompositeTransformation()
                                .add(RotateXTransformation.withGradAngle(-90))
                                .add(RotateYTransformation.withGradAngle(180))
//                        .add(RotateZTransformation.withGradAngle(30))
                                .add(RotateYTransformation.withGradAngle(5))
                                .add(new ScaleTransformation(0.7, 0.7, 0.7))
                )
//                .setIor(IoR.WINDOW_GLASS)
                .setKs(new FloatVec4(0.5f, 0.5f, 0.5f))
//                .setKr(new FloatVec4(0.2f, 0.2f, 0.2f))
                .setPhongExp(1000);
    }
}
