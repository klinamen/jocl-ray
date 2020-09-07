package io.klinamen.joclray.light;

import io.klinamen.joclray.util.FloatVec4;

public class PointLight {
    private float intensity;
    private FloatVec4 position;

    public float getIntensity() {
        return intensity;
    }

    public PointLight setIntensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    public FloatVec4 getPosition() {
        return position;
    }

    public PointLight setPosition(FloatVec4 position) {
        this.position = position;
        return this;
    }
}

