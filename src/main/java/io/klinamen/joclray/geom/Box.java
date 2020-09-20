package io.klinamen.joclray.geom;

import io.klinamen.joclray.util.FloatVec4;

public class Box extends Surface {
    private FloatVec4 vertexMin;
    private FloatVec4 vertexMax;

    public FloatVec4 getVertexMin() {
        return vertexMin;
    }

    public Box(FloatVec4 vertexMin, FloatVec4 vertexMax) {
        this.vertexMin = vertexMin;
        this.vertexMax = vertexMax;
    }

    public Box() {
        this.vertexMin = new FloatVec4();
        this.vertexMax = new FloatVec4();
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

