package io.klinamen.joclray.geom.bvh;

import io.klinamen.joclray.geom.AABB;
import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.util.FloatVec4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OctreeBuilder {
    private final int maxDepth;
    private final int subdivisions;

    public OctreeBuilder(int subdivisions, int maxDepth) {
        this.subdivisions = subdivisions;
        this.maxDepth = maxDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getChildrenNum() {
        return subdivisions * subdivisions * subdivisions;
    }

    public int getTreeSize(){
        return (int)(1 - Math.pow(getChildrenNum(), getMaxDepth() + 1)) / (1 - getChildrenNum());
    }

    public BVNode build(Iterable<TriangleMesh> meshes) {
        AABB bb = null;
        List<BVNode> children = new ArrayList<>();
        for (TriangleMesh mesh : meshes) {
            BVNode child = build(mesh);
            bb = child.getBoundingBox().merge(bb);
            children.add(child);
        }

        return new BVNode(bb).addChildren(children);
    }

    public BVNode build(TriangleMesh mesh) {
        return build(mesh.getFaceSet(), mesh.getBoundingBox(), 0);
    }

    private BVNode build(FaceSet faceSet, AABB bb, int depth) {
        if (depth == maxDepth) {
            return new BVNode(bb).setElement(faceSet);
        }

        FloatVec4 min = bb.getVertexMin().min(bb.getVertexMax());
        FloatVec4 q = bb.getLength().div(subdivisions);

        List<AABB> voxels = new ArrayList<>();

        Map<AABB, FaceSet> parts = new HashMap<>();

        for (int u = 0; u < subdivisions; u++) {
            float uMin = min.getX() + u * q.getX();
            float uMax = uMin + q.getX();
            for (int v = 0; v < subdivisions; v++) {
                float vMin = min.getY() + v * q.getY();
                float vMax = vMin + q.getY();
                for (int j = 0; j < subdivisions; j++) {
                    float jMin = min.getZ() + j * q.getZ();
                    float jMax = jMin + q.getZ();

                    AABB voxel = new AABB(new FloatVec4(uMin, vMin, jMin), new FloatVec4(uMax, vMax, jMax));
                    voxels.add(voxel);

                    // pair overlapping faces to voxel
                    for (Face f : faceSet.getFaces()) {
                        if(f.intersects(voxel)){
                            parts.computeIfAbsent(voxel, k -> new FaceSet()).addFace(f);
                        }
                    }
                }
            }
        }

        BVNode node = new BVNode(bb);

        for (AABB voxel : voxels) {
            FaceSet fs = parts.get(voxel);
            BVNode child;
            if(fs == null){
                child = buildEmpty(voxel, depth + 1);
            } else {
                child = build(fs, voxel, depth + 1);
            }

            node.addChild(child);
        }

        return node;
    }

    private BVNode buildEmpty(AABB bb, int depth){
        if (depth == maxDepth) {
            return new BVNode(bb);
        }

        var node = new BVNode(bb);
        for (int i = 0; i < getChildrenNum(); i++) {
            node.addChild(buildEmpty(bb, depth + 1));
        }

        return node;
    }
}
