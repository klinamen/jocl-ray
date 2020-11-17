package io.klinamen.joclray.materials;

import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.IoR;

public class Glass implements Material {
    private FloatVec4 kr = new FloatVec4();
    private FloatVec4 kt = new FloatVec4();
    private float ior = IoR.WINDOW_GLASS;

    public FloatVec4 getKr() {
        return kr;
    }

    public Glass setKr(FloatVec4 kr) {
        this.kr = kr;
        return this;
    }

    public FloatVec4 getKt() {
        return kt;
    }

    public Glass setKt(FloatVec4 kt) {
        this.kt = kt;
        return this;
    }

    public float getIor() {
        return ior;
    }

    public Glass setIor(float ior) {
        this.ior = ior;
        return this;
    }
}
