package io.klinamen.joclray.geom;

import io.klinamen.joclray.FloatVec4;

public abstract class Surface {
    private FloatVec4 kd = new FloatVec4();
    private FloatVec4 ks = new FloatVec4();
    private float phongExp;

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

    public float getPhongExp() {
        return phongExp;
    }

    public Surface setPhongExp(float phongExp) {
        this.phongExp = phongExp;
        return this;
    }
}
