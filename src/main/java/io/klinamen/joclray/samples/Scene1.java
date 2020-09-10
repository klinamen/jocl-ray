package io.klinamen.joclray.samples;

import io.klinamen.joclray.geom.Box;
import io.klinamen.joclray.geom.Plane;
import io.klinamen.joclray.geom.Sphere;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.light.SpotLight;
import io.klinamen.joclray.scene.Camera;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;

public class Scene1 {
    public static Scene build() {
                return new Scene(new Camera()
//                .setFrameWidth(image.getWidth())
//                .setFrameHeight(image.getHeight())
                .setFovGrad(50)
        )
                .setAmbientLightIntensity(0.2f)
                .add(new PointLight()
                        .setIntensity(0.8f)
                        .setPosition(new FloatVec4(-12, 15, -10))
                )
                .add(new SpotLight()
                        .setIntensity(3f)
                        .setPosition(new FloatVec4(-3, 5, 0))
                        .setAngleGrad(20)
                        .setDirection(new FloatVec4(0.5f, -0.3f, -1))
                )
                .add(new PointLight()
                        .setIntensity(0.5f)
                        .setPosition(new FloatVec4(12, 8, 0))
                )
                .add("Red_Sphere", new Sphere()
                        .setCenter(new FloatVec4(2, 0, -40))
                        .setRadius(10)
                        .setKd(new FloatVec4(0.5f, 0, 0))
                        .setKs(new FloatVec4(0.5f, 0.5f, 0.5f))
                        .setKr(new FloatVec4(0.8f, 0.8f, 0.8f))
                        .setPhongExp(500)
                )
                .add("Green_Sphere", new Sphere()
                        .setCenter(new FloatVec4(-5, 0, -20))
                        .setRadius(5)
                        .setKd(new FloatVec4(0, 0.5f, 0))
                        .setKs(new FloatVec4(0.8f, 0.8f, 0.8f))
                        .setKr(new FloatVec4(0.5f, 0.5f, 0.5f))
                        .setPhongExp(100)
                )
                .add("Blue_Sphere", new Sphere()
                        .setCenter(new FloatVec4(6, -2, -25))
                        .setRadius(3)
                        .setKd(new FloatVec4(0, 0f, 0.5f))
                        .setKs(new FloatVec4(0.5f, 0.5f, 0.5f))
                        .setKr(new FloatVec4(0.4f, 0.4f, 0.2f))
                        .setPhongExp(800)
                )
                .add("Box_Mirror", new Box()
                        .setVertexMin(new FloatVec4(13.5f, -5, -21))
                        .setVertexMax(new FloatVec4(13.7f, 5, -30))
                        .setKd(new FloatVec4(0, 0.3f, 0.3f))
                        .setKs(new FloatVec4(0.3f, 0.3f, 0.3f))
                        .setKr(new FloatVec4(0.4f, 0.4f, 0.7f))
                        .setPhongExp(1000)
                )
                .add("Ceiling", new Plane()
                        .setNormal(new FloatVec4(0, -1, 0))
                        .setPosition(new FloatVec4(0, 20, 0))
                        .setKd(new FloatVec4(0.752f, 0.901f, 0.925f))
                )
                .add("Floor", new Plane()
                        .setNormal(new FloatVec4(0, 1, 0))
                        .setPosition(new FloatVec4(0, -5, 0))
                        .setKd(new FloatVec4(0.3f, 0.2f, 0.3f))
                        .setKs(new FloatVec4(0.3f, 0.3f, 0.3f))
                        .setKr(new FloatVec4(0.2f, 0.2f, 0.2f))
                        .setPhongExp(10)
                )
                .add("Left_Wall", new Plane()
                        .setNormal(new FloatVec4(1, 0, 0))
                        .setPosition(new FloatVec4(-15, 0, 0))
                        .setKd(new FloatVec4(0.3f, 0.2f, 0.3f))
                        .setPhongExp(1000)
                )
                .add("Right_Wall", new Plane()
                        .setNormal(new FloatVec4(-1, 0, 0))
                        .setPosition(new FloatVec4(30, 0, 0))
                        .setKd(new FloatVec4(0.3f, 0.2f, 0.3f))
                        .setPhongExp(1000)
                )
                ;
    }
}
