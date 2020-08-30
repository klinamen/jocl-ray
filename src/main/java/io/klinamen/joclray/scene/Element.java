package io.klinamen.joclray.scene;

public abstract class Element {
    private final int id;

    public Element(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

