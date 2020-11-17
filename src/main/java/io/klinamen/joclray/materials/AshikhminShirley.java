package io.klinamen.joclray.materials;

import io.klinamen.joclray.util.FloatVec4;

public class AshikhminShirley implements Material {
    private FloatVec4 kd = new FloatVec4();
    private FloatVec4 kr = new FloatVec4();
    private float nu;
    private float nv;

    public FloatVec4 getKd() {
        return kd;
    }

    public AshikhminShirley setKd(FloatVec4 kd) {
        this.kd = kd;
        return this;
    }

    public FloatVec4 getKr() {
        return kr;
    }

    public AshikhminShirley setKr(FloatVec4 kr) {
        this.kr = kr;
        return this;
    }

    public float getNu() {
        return nu;
    }

    public AshikhminShirley setNu(float nu) {
        this.nu = nu;
        return this;
    }

    public float getNv() {
        return nv;
    }

    public AshikhminShirley setNv(float nv) {
        this.nv = nv;
        return this;
    }
}
