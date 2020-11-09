package io.klinamen.joclray.tonemapping;

import io.klinamen.joclray.util.FloatVec4;

public class HableFilmicToneOperator implements ToneMappingOperator {
    private final static float A = 0.15f;
    private final static float B = 0.50f;
    private final static float C = 0.10f;
    private final static float D = 0.20f;
    private final static float E = 0.02f;
    private final static float F = 0.30f;

    private final float exposureBias;
    private final float w;

    public HableFilmicToneOperator() {
        this.exposureBias = 2.0f;
        this.w = 11.2f;
    }

    public HableFilmicToneOperator(float exposureBias, float w) {
        this.exposureBias = exposureBias;
        this.w = w;
    }

    private FloatVec4 hableFilmicMap(FloatVec4 x) {
        return x.mul(x.mul(A).plus(C * B)).plus(D * E)
                .div(x.mul(x.mul(A).plus(B)).plus(D * F))
                .minus(E / F);
    }

    @Override
    public FloatVec4 toneMap(FloatVec4 radiance) {
        FloatVec4 v = hableFilmicMap(radiance.mul(exposureBias));
        FloatVec4 whiteScale = FloatVec4.ONE.div(hableFilmicMap(FloatVec4.all(w)));
        return v.mul(whiteScale);
    }
}
