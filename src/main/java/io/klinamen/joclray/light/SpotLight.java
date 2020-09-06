package io.klinamen.joclray.light;

import io.klinamen.joclray.FloatVec4;

public class SpotLight extends PointLight {
    private float angleGrad;
    private FloatVec4 direction;

    public float getAngleGrad() {
        return angleGrad;
    }

    public SpotLight setAngleGrad(float angleGrad) {
        this.angleGrad = angleGrad;
        return this;
    }

    public FloatVec4 getDirection() {
        return direction;
    }

    public SpotLight setDirection(FloatVec4 direction) {
        this.direction = direction;
        return this;
    }

    public float getAngleRad(){
        return (float)(angleGrad * Math.PI / 180);
    }

    @Override
    public SpotLight setIntensity(float intensity) {
        super.setIntensity(intensity);
        return this;
    }

    @Override
    public SpotLight setPosition(FloatVec4 position) {
        super.setPosition(position);
        return this;
    }
}
