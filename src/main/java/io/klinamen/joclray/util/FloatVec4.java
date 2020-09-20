package io.klinamen.joclray.util;

import java.util.Collection;

public class FloatVec4 {
    public static int DIM = 4;

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

    public FloatVec4 sum(FloatVec4 v1) {
        return new FloatVec4(getX() + v1.getX(), getY() + v1.getY(), getZ() + v1.getZ(), getW() + v1.getW());
    }

    public FloatVec4 mul(float s) {
        return new FloatVec4(s * getX(), s * getY(), s * getZ(), s * getW());
    }

    public FloatVec4 minus(FloatVec4 v1) {
        return new FloatVec4(getX() - v1.getX(), getY() - v1.getY(), getZ() - v1.getZ(), getW() - v1.getW());
    }

    public float length() {
        return (float) Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ() + getW() * getW());
    }

    public static FloatVec4 extract(float[] aValues, int offset) {
        if (aValues.length - offset < FloatVec4.DIM) {
            throw new IllegalArgumentException("Input array length exceeded.");
        }

        return new FloatVec4(aValues[offset], aValues[offset + 1], aValues[offset + 2], aValues[offset + 3]);
    }

    public static float[] flatten(Collection<FloatVec4> values){
        float[] out = new float[values.size() * FloatVec4.DIM];
        int i=0;
        for (FloatVec4 item : values) {
            System.arraycopy(item.getArray(), 0, out, i * FloatVec4.DIM, FloatVec4.DIM);
            i++;
        }
        return out;
    }
}
