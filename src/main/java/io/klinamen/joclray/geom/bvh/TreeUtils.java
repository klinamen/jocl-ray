package io.klinamen.joclray.geom.bvh;

import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

public class TreeUtils {
    public static <T extends Tree<T>> void bfVisit(Tree<T> node, Consumer<Tree<T>> action) {
        var q = new LinkedList<Tree<T>>();
        q.add(node);
        while (!q.isEmpty()) {
            Tree<T> n = q.poll();
            action.accept(n);
            n.getChildren().forEach(q::add);
        }
    }

    public static <T extends Tree<T>> SortedMap<Integer, T> bfIndexTree(Tree<T> tree) {
        SortedMap<Integer, T> treeIndex = new TreeMap<>();

        var ref = new Object() {
            int nodeId = 0;
        };

        TreeUtils.bfVisit(tree, n -> treeIndex.put(ref.nodeId++, n.get()));

        return treeIndex;
    }
}
