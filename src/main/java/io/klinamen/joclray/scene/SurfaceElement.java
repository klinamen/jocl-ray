package io.klinamen.joclray.scene;

import io.klinamen.joclray.geom.Surface;

public class SurfaceElement<T extends Surface> extends Element {
    private final T surface;

    public SurfaceElement(int id, T surface) {
        super(id);
        this.surface = surface;
    }

    public T getSurface() {
        return surface;
    }
}
