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
            FloatVec4 radiance = FloatVec4.extract(colors, i * FloatVec4.DIM);

            // apply tone mapping
            FloatVec4 tmRadiance = toneMappingOperator.toneMap(radiance);

            if(tmRadiance.maxComponent() > 1.0f){
                System.out.println(String.format("Overshooting rad at %d: %f, %f, %f", i, radiance.getX(), radiance.getY(), radiance.getZ()));
            }

            int x = i % image.getWidth();
            int y = i / image.getWidth();
            image.getRaster().setPixel(x, y, toRgb(tmRadiance));
        }
    }

    protected int[] toRgb(FloatVec4 radiance){
        return new int[]{
                (int) (255 * radiance.getX()),      // R spectral radiance
                (int) (255 * radiance.getY()),      // G spectral radiance
                (int) (255 * radiance.getZ())       // B spectral radiance
        };
    }
}

