package io.klinamen.joclray.materials;

import io.klinamen.joclray.util.FloatVec4;

public class ProceduralWood implements Material {
    private FloatVec4 kd = new FloatVec4();

    public FloatVec4 getKd() {
        return kd;
    }

    public ProceduralWood setKd(FloatVec4 kd) {
        this.kd = kd;
        return this;
    }
}

