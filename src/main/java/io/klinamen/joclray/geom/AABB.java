package io.klinamen.joclray.geom;

import io.klinamen.joclray.util.FloatVec4;

public class AABB {
    private static float EPSILON = 0.001f;

    private final FloatVec4 vertexMin;
    private final FloatVec4 vertexMax;

    public FloatVec4 getVertexMin() {
        return vertexMin;
    }

    public AABB(FloatVec4 vertexMin, FloatVec4 vertexMax) {
        this.vertexMin = vertexMin;
        this.vertexMax = vertexMax;
    }

    public FloatVec4 getVertexMax() {
        return vertexMax;
    }

    public AABB merge(AABB other){
        if(other == null)
            return this;

        return new AABB(this.vertexMin.min(other.getVertexMin()), this.vertexMax.max(other.getVertexMax()));
    }

    public FloatVec4 getLength(){
        return vertexMax.minus(vertexMin).abs();
    }

    public FloatVec4 getCenter(){
        return vertexMin.min(vertexMax)
            .sum(vertexMax.minus(vertexMin).abs().div(2))
        ;
    }

    public FloatVec4 getExtents(){
        return getLength().div(2);
    }

    public boolean contains(FloatVec4 point){
        if(point.getX() < vertexMin.getX() - EPSILON || point.getX() > vertexMax.getX() + EPSILON)
            return false;

        if(point.getY() < vertexMin.getY() - EPSILON || point.getY() > vertexMax.getY() + EPSILON)
            return false;

        if(point.getZ() < vertexMin.getZ() - EPSILON || point.getZ() > vertexMax.getZ() + EPSILON)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", vertexMin, vertexMax);
    }
}
