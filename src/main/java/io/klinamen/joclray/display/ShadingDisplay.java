package io.klinamen.joclray.display;

import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.util.FloatVec4;

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
                    (int) (255 * clamp(colors[i * FloatVec4.DIM], 0, 1)),
                    (int) (255 * clamp(colors[i * FloatVec4.DIM + 1], 0, 1)),
                    (int) (255 * clamp(colors[i * FloatVec4.DIM + 2], 0, 1)),
            };

            int x = i % (int) scene.getCamera().getFrameWidth();
            int y = i / (int) scene.getCamera().getFrameWidth();
            image.getRaster().setPixel(x, y, pColor);
        }
    }

    private float clamp(float n, float min, float max){
        return Math.max(min, Math.min(n, max));
    }
}