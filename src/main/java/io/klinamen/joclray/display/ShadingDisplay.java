package io.klinamen.joclray.display;

import io.klinamen.joclray.FloatVec4;
import io.klinamen.joclray.scene.Scene;

import java.awt.image.BufferedImage;

public class ShadingDisplay {
    private final Scene scene;
    private final float[] colors;

    public ShadingDisplay(Scene scene, float[] colors) {
        this.scene = scene;
        this.colors = colors;
    }

    public void update(BufferedImage image) {
        int nPixels = (int) scene.getCamera().getFrameWidth() * (int) scene.getCamera().getFrameHeight();

        for (int i = 0; i < nPixels; i++) {
            int[] pColor = new int[]{
                    (int) (255 * colors[i * FloatVec4.DIM]),
                    (int) (255 * colors[i * FloatVec4.DIM + 1]),
                    (int) (255 * colors[i * FloatVec4.DIM + 2]),
            };

            int x = i % (int) scene.getCamera().getFrameWidth();
            int y = i / (int) scene.getCamera().getFrameWidth();
            image.getRaster().setPixel(x, y, pColor);
        }
    }
}
