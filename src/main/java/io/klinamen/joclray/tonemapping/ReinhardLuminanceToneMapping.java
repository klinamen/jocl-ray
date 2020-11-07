package io.klinamen.joclray.tonemapping;

import io.klinamen.joclray.util.FloatVec4;

public class ReinhardLuminanceToneMapping implements ToneMappingOperator {
    private static final FloatVec4 rgbToLum = new FloatVec4(0.2126f, 0.7152f, 0.0722f);

    private final float maxLuminance;

    public ReinhardLuminanceToneMapping(float maxLuminance) {
        this.maxLuminance = maxLuminance;
    }

    @Override
    public FloatVec4 toneMap(FloatVec4 radiance) {
        float lOld = luminance(radiance);
        float num = lOld * (1.0f + (lOld / (maxLuminance * maxLuminance)));
        float lNew = num / (1.0f + lOld);
        return new ClampToneMapping().toneMap(changeLuminance(radiance, lNew));
    }

    private static float luminance(FloatVec4 rgb) {
        return rgb.dot(rgbToLum);
    }

    private static FloatVec4 changeLuminance(FloatVec4 c, float lOut) {
        return c.mul(lOut / luminance(c));
    }

    public static ReinhardLuminanceToneMapping from(float[] radiance) {
        float maxLuminance = 0;
        for (int i = 0; i < radiance.length / FloatVec4.DIM; i++) {
            FloatVec4 r = FloatVec4.extract(radiance, i * FloatVec4.DIM);
            float luminance = luminance(r);
            if(!Float.isNaN(luminance)) {
                maxLuminance = Math.max(maxLuminance, luminance);
            } else {
                System.out.println(String.format("Skipping NaN luminance at pixel %d", i));
            }
        }

        return new ReinhardLuminanceToneMapping(maxLuminance);
    }
}
