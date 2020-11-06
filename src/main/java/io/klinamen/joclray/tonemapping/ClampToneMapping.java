package io.klinamen.joclray.tonemapping;

public class ClampToneMapping implements ToneMappingOperator {
    @Override
    public int[] toneMap(float r, float g, float b) {
        return new int[]{
                (int) (255 * clamp(r, 0, 1)),
                (int) (255 * clamp(g, 0, 1)),
                (int) (255 * clamp(b, 0, 1)),
        };
    }

    private float clamp(float n, float min, float max) {
        return Math.max(min, Math.min(n, max));
    }
}
