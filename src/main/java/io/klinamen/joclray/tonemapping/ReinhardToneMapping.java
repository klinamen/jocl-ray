package io.klinamen.joclray.tonemapping;

import io.klinamen.joclray.util.FloatVec4;

public class ReinhardToneMapping implements ToneMappingOperator {
    @Override
    public FloatVec4 toneMap(FloatVec4 radiance) {
        return radiance.apply(v -> v / (v + 1));
    }
}
