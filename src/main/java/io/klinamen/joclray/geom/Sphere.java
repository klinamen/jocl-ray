package io.klinamen.joclray.geom;

import io.klinamen.joclray.FloatVec4;

public class Sphere extends Surface {
    private float radius;
    private FloatVec4 center = new FloatVec4();

    public float getRadius() {
        return radius;
    }

    public Sphere setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public FloatVec4 getCenter() {
        return center;
    }

    public Sphere setCenter(FloatVec4 center) {
        this.center = center;
        return this;
    }
}

