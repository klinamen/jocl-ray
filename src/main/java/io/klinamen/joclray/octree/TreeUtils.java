package io.klinamen.joclray.octree;

import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

public class TreeUtils {
    public static <T extends Tree<T>> void breadthFirstVisit(Tree<T> node, Consumer<Tree<T>> action) {
        var q = new LinkedList<Tree<T>>();
        q.add(node);
        while (!q.isEmpty()) {
            Tree<T> n = q.poll();
            action.accept(n);
            n.getChildren().forEach(q::add);
        }
    }

    public static <T extends Tree<T>> void depthFirstVisit(Tree<T> node, Consumer<Tree<T>> action) {
        action.accept(node);
        for (T child : node.getChildren()) {
            depthFirstVisit(child, action);
        }
    }

    public static <T extends Tree<T>> SortedMap<Integer, T> bfIndexTree(Tree<T> tree) {
        SortedMap<Integer, T> treeIndex = new TreeMap<>();

        var ref = new Object() {
            int nodeId = 0;
        };

        TreeUtils.breadthFirstVisit(tree, n -> treeIndex.put(ref.nodeId++, n.get()));

        return treeIndex;
    }
}
