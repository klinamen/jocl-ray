package io.klinamen.joclray.geom;

import io.klinamen.joclray.util.FloatVec4;

public class Box extends Surface {
    private FloatVec4 vertexMin = new FloatVec4();
    private FloatVec4 vertexMax = new FloatVec4();

    public FloatVec4 getVertexMin() {
        return vertexMin;
    }

    public Box setVertexMin(FloatVec4 vertexMin) {
        this.vertexMin = vertexMin;
        return this;
    }

    public FloatVec4 getVertexMax() {
        return vertexMax;
    }

    public Box setVertexMax(FloatVec4 vertexMax) {
        this.vertexMax = vertexMax;
        return this;
    }
}
