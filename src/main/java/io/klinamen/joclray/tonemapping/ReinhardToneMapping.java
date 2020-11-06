package io.klinamen.joclray.tonemapping;

public class ReinhardToneMapping implements ToneMappingOperator {
    @Override
    public int[] toneMap(float r, float g, float b) {
        return new int[]{
                (int) (255 * transform(r)),
                (int) (255 * transform(g)),
                (int) (255 * transform(b))
        };
    }

    private float transform(float v) {
        return v / (v + 1);
    }
}
