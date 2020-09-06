package io.klinamen.joclray.scene;

public abstract class Element {
    private final String name;
    private final int id;

    public Element(int id) {
        this.id = id;
        this.name = null;
    }

    public Element(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

