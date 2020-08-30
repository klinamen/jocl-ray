package io.klinamen.joclray;

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
}
