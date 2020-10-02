package io.klinamen.joclray.octree;

public interface Tree<T extends Tree<T>> {
    Iterable<T> getChildren();
    T get();
}
