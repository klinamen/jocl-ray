package io.klinamen.joclray.geom;

import io.klinamen.joclray.FloatVec4;

public class Box extends Surface {
    private FloatVec4 vMin = new FloatVec4();
    private FloatVec4 vMax = new FloatVec4();

    public FloatVec4 getvMin() {
        return vMin;
    }

    public Box setvMin(FloatVec4 vMin) {
        this.vMin = vMin;
        return this;
    }

    public FloatVec4 getvMax() {
        return vMax;
    }

    public Box setvMax(FloatVec4 vMax) {
        this.vMax = vMax;
        return this;
    }
}
