package io.klinamen.joclray.rendering;

import io.klinamen.joclray.scene.Scene;

import java.awt.image.BufferedImage;

public interface Renderer {
    void render(Scene scene, BufferedImage outImage);
}
