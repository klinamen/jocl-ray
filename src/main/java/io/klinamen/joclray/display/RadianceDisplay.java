package io.klinamen.joclray.display;

import io.klinamen.joclray.tonemapping.ClampToneMapping;
import io.klinamen.joclray.tonemapping.ToneMappingOperator;
import io.klinamen.joclray.util.FloatVec4;

import java.awt.image.BufferedImage;

public class RadianceDisplay {
    private final ToneMappingOperator toneMappingOperator;

    public RadianceDisplay() {
        this.toneMappingOperator = new ClampToneMapping();
    }

    public RadianceDisplay(ToneMappingOperator toneMappingOperator) {
        this.toneMappingOperator = toneMappingOperator;
    }

    public void display(float[] colors, BufferedImage image) {
        int nPixels = colors.length / FloatVec4.DIM;

        for (int i = 0; i < nPixels; i++) {
            int[] pColor = toneMappingOperator.toneMap(
                    colors[i * FloatVec4.DIM],      // R spectral radiance
                    colors[i * FloatVec4.DIM + 1],  // G spectral radiance
                    colors[i * FloatVec4.DIM + 2]   // B spectral radiance
            );

            int x = i % image.getWidth();
            int y = i / image.getWidth();
            image.getRaster().setPixel(x, y, pColor);
        }
    }
}

