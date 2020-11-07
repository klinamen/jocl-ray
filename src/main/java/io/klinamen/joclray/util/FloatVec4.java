package io.klinamen.joclray.util;

import java.util.Collection;
import java.util.function.Function;

public class FloatVec4 {
    public static final int DIM = 4;
    public static final FloatVec4 ONE = new FloatVec4(1,1,1,1);

    private final float[] v = new float[]{0, 0, 0, 0};

    public FloatVec4(float x, float y, float z) {
        v[0] = x;
        v[1] = y;
        v[2] = z;
    }

    public FloatVec4(float x, float y, float z, float w) {
        this(x, y, z);
        v[3] = w;
    }

    public FloatVec4() {
    }

    public float[] getArray() {
        return v;
    }

    public float getX() {
        return v[0];
    }

    public float getY() {
        return v[1];
    }

    public float getZ() {
        return v[2];
    }

    public float getW() {
        return v[3];
    }

//    public FloatVec4 setX(float x) {
//        v[0] = x;
//        return this;
//    }
//
//    public FloatVec4 setY(float y) {
//        v[1] = y;
//        return this;
//    }
//
//    public FloatVec4 setZ(float z) {
//        v[2] = z;
//        return this;
//    }
//
//    public FloatVec4 setW(float w) {
//        v[3] = w;
//        return this;
//    }

    public FloatVec4 sum(FloatVec4 v1) {
        return new FloatVec4(getX() + v1.getX(), getY() + v1.getY(), getZ() + v1.getZ(), getW() + v1.getW());
    }

    public FloatVec4 mul(float s) {
        return new FloatVec4(s * getX(), s * getY(), s * getZ(), s * getW());
    }

    public FloatVec4 mul(FloatVec4 other){
        return new FloatVec4(getX() * other.getX(), getY() * other.getY(), getZ() * other.getZ(), getW() * other.getW());
    }

    public FloatVec4 invert(){
        return new FloatVec4(1/getX(), 1/getY(), 1/getZ(), 1/getW());
    }

    public FloatVec4 div(float s) {
        return new FloatVec4(getX() / s, getY() / s, getZ() / s, getW() / s);
    }

    public FloatVec4 div(FloatVec4 other){
        return new FloatVec4(getX() / other.getX(), getY() / other.getY(), getZ() / other.getZ(), getW() / other.getW());
    }

    public FloatVec4 minus(FloatVec4 v1) {
        return new FloatVec4(getX() - v1.getX(), getY() - v1.getY(), getZ() - v1.getZ(), getW() - v1.getW());
    }

    public FloatVec4 copy(){
        return new FloatVec4(getX(), getY(), getZ(), getW());
    }

    public FloatVec4 min(FloatVec4 v1) {
        if(v1 == null)
            return this;

        return new FloatVec4(
                Math.min(getX(), v1.getX()),
                Math.min(getY(), v1.getY()),
                Math.min(getZ(), v1.getZ()),
                Math.min(getW(), v1.getW())
        );
    }

    public FloatVec4 max(FloatVec4 v1) {
        if(v1 == null)
            return this;

        return new FloatVec4(
                Math.max(getX(), v1.getX()),
                Math.max(getY(), v1.getY()),
                Math.max(getZ(), v1.getZ()),
                Math.max(getW(), v1.getW())
        );
    }

    public FloatVec4 abs(){
        return new FloatVec4(
                Math.abs(getX()),
                Math.abs(getY()),
                Math.abs(getZ()),
                Math.abs(getW())
        );
    }

    public float dot(FloatVec4 v){
        return this.getX() * v.getX() + this.getY() * v.getY() + this.getZ() * v.getZ() + this.getW() * v.getW();
    }

    public FloatVec4 cross(FloatVec4 v){
        return new FloatVec4(
          this.getY() * v.getZ() - this.getZ() * v.getY(),
          this.getZ() * v.getX() - this.getX() * v.getZ(),
          this.getX() * v.getY() - this.getY() * v.getX(),
          0
        );
    }

    public float length() {
        return (float) Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ() + getW() * getW());
    }

    public float maxComponent() {
        return Math.max(Math.max(Math.max(getX(), getY()), getZ()), getW());
    }

    public FloatVec4 apply(Function<Float,Float> fn){
        return new FloatVec4(
                fn.apply(getX()), fn.apply(getY()), fn.apply(getZ()), fn.apply(getW())
        );
    }

    public static FloatVec4 extract(float[] aValues, int offset) {
        if (aValues.length - offset < FloatVec4.DIM) {
            throw new IllegalArgumentException("Input array length exceeded.");
        }

        return new FloatVec4(aValues[offset], aValues[offset + 1], aValues[offset + 2], aValues[offset + 3]);
    }

    public static float[] flatten(Collection<FloatVec4> values) {
        float[] out = new float[values.size() * FloatVec4.DIM];
        int i = 0;
        for (FloatVec4 item : values) {
            System.arraycopy(item.getArray(), 0, out, i * FloatVec4.DIM, FloatVec4.DIM);
            i++;
        }
        return out;
    }

    @Override
    public String toString() {
        return String.format("(%2f, %2f, %2f, %2f)", getX(), getY(), getZ(), getW());
    }
}
