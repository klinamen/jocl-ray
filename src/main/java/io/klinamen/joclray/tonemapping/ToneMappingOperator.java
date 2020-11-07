package io.klinamen.joclray.tonemapping;

import io.klinamen.joclray.util.FloatVec4;

@FunctionalInterface
public interface ToneMappingOperator {
    FloatVec4 toneMap(FloatVec4 radiance);
}
