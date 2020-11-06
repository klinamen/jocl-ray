package io.klinamen.joclray.display;

import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.tonemapping.ClampToneMapping;
import io.klinamen.joclray.tonemapping.ToneMappingOperator;
import io.klinamen.joclray.util.FloatVec4;

import java.awt.image.BufferedImage;

public class ShadingDisplay {
    private final Scene scene;
    private final float[] colors;
    private final ToneMappingOperator toneMappingOperator;

    public ShadingDisplay(Scene scene, float[] colors) {
        this.scene = scene;
        this.colors = colors;
        this.toneMappingOperator = new ClampToneMapping();
    }

    public ShadingDisplay(Scene scene, float[] colors, ToneMappingOperator toneMappingOperator) {
        this.scene = scene;
        this.colors = colors;
        this.toneMappingOperator = toneMappingOperator;
    }

    public void update(BufferedImage image) {
        int nPixels = (int) scene.getCamera().getFrameWidth() * (int) scene.getCamera().getFrameHeight();

        for (int i = 0; i < nPixels; i++) {
            int[] pColor = toneMappingOperator.toneMap(
                    colors[i * FloatVec4.DIM],      // R spectral radiance
                    colors[i * FloatVec4.DIM + 1],  // G spectral radiance
                    colors[i * FloatVec4.DIM + 2]   // B spectral radiance
            );

            int x = i % (int) scene.getCamera().getFrameWidth();
            int y = i / (int) scene.getCamera().getFrameWidth();
            image.getRaster().setPixel(x, y, pColor);
        }
    }
}

