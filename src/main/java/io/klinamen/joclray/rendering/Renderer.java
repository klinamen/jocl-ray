package io.klinamen.joclray.rendering;

import io.klinamen.joclray.scene.Scene;

public interface Renderer {
    float[] render(Scene scene);
}
