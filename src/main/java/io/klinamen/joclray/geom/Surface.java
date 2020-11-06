package io.klinamen.joclray.geom;

import io.klinamen.joclray.util.FloatVec4;

public abstract class Surface {
    private FloatVec4 kd = new FloatVec4();
    private FloatVec4 ks = new FloatVec4();
    private FloatVec4 kr = new FloatVec4();
    private float phongExp;
    private float ior;

    private FloatVec4 emission = new FloatVec4();

    public FloatVec4 getKd() {
        return kd;
    }

    public Surface setKd(FloatVec4 kd) {
        this.kd = kd;
        return this;
    }

    public FloatVec4 getKs() {
        return ks;
    }

    public Surface setKs(FloatVec4 ks) {
        this.ks = ks;
        return this;
    }

    public FloatVec4 getKr() {
        return kr;
    }

    public Surface setKr(FloatVec4 kr) {
        this.kr = kr;
        return this;
    }

    public float getPhongExp() {
        return phongExp;
    }

    public Surface setPhongExp(float phongExp) {
        this.phongExp = phongExp;
        return this;
    }

    public float getIor() {
        return ior;
    }

    public Surface setIor(float ior) {
        this.ior = ior;
        return this;
    }

    public FloatVec4 getEmission() {
        return emission;
    }

    public Surface setEmission(FloatVec4 emission) {
        this.emission = emission;
        return this;
    }
}
