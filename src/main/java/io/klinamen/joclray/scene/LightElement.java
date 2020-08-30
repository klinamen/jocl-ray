package io.klinamen.joclray.scene;

import io.klinamen.joclray.light.PointLight;

public class LightElement extends Element {
    private final PointLight light;

    public LightElement(int id, PointLight light) {
        super(id);
        this.light = light;
    }

    public PointLight getLight() {
        return light;
    }
}
