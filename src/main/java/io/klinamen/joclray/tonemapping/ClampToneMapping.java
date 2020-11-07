package io.klinamen.joclray.tonemapping;

import io.klinamen.joclray.util.FloatVec4;

public class ClampToneMapping implements ToneMappingOperator {
    private float clamp(float n, float min, float max) {
        return Math.max(min, Math.min(n, max));
    }

    @Override
    public FloatVec4 toneMap(FloatVec4 radiance) {
        return radiance.apply(v -> clamp(v, 0, 1));
    }
}
