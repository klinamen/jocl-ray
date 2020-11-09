package io.klinamen.joclray.tonemapping;

import io.klinamen.joclray.util.FloatVec4;

public class ExtendedReinhardToneMapping implements ToneMappingOperator {
    private final FloatVec4 maxRadiance;

    public ExtendedReinhardToneMapping(FloatVec4 maxRadiance) {
        this.maxRadiance = maxRadiance;
    }

    @Override
    public FloatVec4 toneMap(FloatVec4 radiance) {
        return radiance
                .mul(FloatVec4.ONE.plus(radiance.div(maxRadiance.mul(maxRadiance))))
                .div(FloatVec4.ONE.plus(radiance));
    }

    public static ExtendedReinhardToneMapping from(float[] radiance) {
        FloatVec4 maxRadiance = new FloatVec4();
        for (int i = 0; i < radiance.length / FloatVec4.DIM; i++) {
            FloatVec4 r = FloatVec4.extract(radiance, i * FloatVec4.DIM);
            if(!(Float.isNaN(r.getX()) || Float.isNaN(r.getY()) || Float.isNaN(r.getZ()))) {
                maxRadiance = maxRadiance.max(r);
            } else {
                System.out.println(String.format("Skipping NaN radiance at pixel %d", i));
            }

        }

        return new ExtendedReinhardToneMapping(maxRadiance);
    }
}
