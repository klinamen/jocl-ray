package io.klinamen.joclray.tonemapping;

@FunctionalInterface
public interface ToneMappingOperator {
    int[] toneMap(float r, float g, float b);
}
