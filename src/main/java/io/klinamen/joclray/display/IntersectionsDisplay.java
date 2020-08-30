package io.klinamen.joclray.display;

import io.klinamen.joclray.IntersectResult;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.scene.ElementSet;
import io.klinamen.joclray.scene.Scene;
import io.klinamen.joclray.scene.SurfaceElement;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class IntersectionsDisplay {
    private final Scene scene;
    private final IntersectResult intersectResult;

    Map<Integer, int[]> colorMap;

    public IntersectionsDisplay(Scene scene, IntersectResult intersectResult) {
        this.scene = scene;
        this.intersectResult = intersectResult;
    }

    public void update(BufferedImage image) {
        int[] hitMap = intersectResult.getHitMap();
        for (int i = 0; i < hitMap.length; i++) {
            if (hitMap[i] >= 0) {
                int x = i % (int) scene.getCamera().getFrameWidth();
                int y = i / (int) scene.getCamera().getFrameWidth();
                image.getRaster().setPixel(x, y, getColor(hitMap[i]));
            }
        }
    }

    private int[] getColor(int id) {
        if (colorMap == null) {
            colorMap = buildColorMap();
        }

        return colorMap.get(id);
    }

    private Map<Integer, int[]> buildColorMap() {
        colorMap = new HashMap<>();
        ElementSet<SurfaceElement<Surface>> surfaces = scene.getSurfaceSetByType(Surface.class);
        final int step = (200 - 100) / surfaces.size();
        for (SurfaceElement<Surface> item : surfaces) {
            int c = 100 + step * colorMap.size();
            colorMap.put(item.getId(), new int[]{c, c, c});
        }
        return colorMap;
    }
}
