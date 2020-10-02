package io.klinamen.joclray.geom.bvh;

import io.klinamen.joclray.geom.AABB;

import java.util.ArrayList;
import java.util.List;

public class BVNode implements Tree<BVNode> {
    private final List<BVNode> children = new ArrayList<>();
    private final AABB boundingBox;
    private FaceSet element;

    public BVNode(FaceSet element) {
        this.element = element;
        this.boundingBox = element.getBoundingBox();
    }

    public BVNode(AABB bb) {
        this.boundingBox = bb;
    }

    public BVNode addChild(BVNode node) {
        children.add(node);
        return this;
    }

    public BVNode addChildren(Iterable<BVNode> nodes) {
        nodes.forEach(this::addChild);
        return this;
    }

    public Iterable<BVNode> getChildren() {
        return children;
    }

    public int getNumChildren(){
        return children.size();
    }

    @Override
    public BVNode get() {
        return this;
    }

    public FaceSet getElement() {
        return element;
    }

    public BVNode setElement(FaceSet element) {
        this.element = element;
        return this;
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }
}
