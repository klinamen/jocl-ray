package io.klinamen.joclray.geom;

import io.klinamen.joclray.util.FloatVec4;

public class Triangle extends Surface {
    private FloatVec4 v0 = new FloatVec4();
    private FloatVec4 v1 = new FloatVec4();
    private FloatVec4 v2 = new FloatVec4();
    private FloatVec4 normal = new FloatVec4();

    public FloatVec4 getV0() {
        return v0;
    }

    public Triangle setV0(FloatVec4 v0) {
        this.v0 = v0;
        return this;
    }

    public FloatVec4 getV1() {
        return v1;
    }

    public Triangle setV1(FloatVec4 v1) {
        this.v1 = v1;
        return this;
    }

    public FloatVec4 getV2() {
        return v2;
    }

    public Triangle setV2(FloatVec4 v2) {
        this.v2 = v2;
        return this;
    }

    public FloatVec4 getNormal() {
        return normal;
    }

    public Triangle setNormal(FloatVec4 normal) {
        this.normal = normal;
        return this;
    }
}
