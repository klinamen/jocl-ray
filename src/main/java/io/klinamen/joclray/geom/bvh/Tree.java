package io.klinamen.joclray.geom.bvh;

public interface Tree<T extends Tree<T>> {
    Iterable<T> getChildren();
    T get();
}
