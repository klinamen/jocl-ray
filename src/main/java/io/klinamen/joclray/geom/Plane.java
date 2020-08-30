package io.klinamen.joclray.geom;

import io.klinamen.joclray.FloatVec4;

public class Plane extends Surface {
    private FloatVec4 position = new FloatVec4();
    private FloatVec4 normal = new FloatVec4();

    public FloatVec4 getPosition() {
        return position;
    }

    public Plane setPosition(FloatVec4 position) {
        this.position = position;
        return this;
    }

    public FloatVec4 getNormal() {
        return normal;
    }

    public Plane setNormal(FloatVec4 normal) {
        this.normal = normal;
        return this;
    }
}
