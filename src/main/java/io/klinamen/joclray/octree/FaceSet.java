package io.klinamen.joclray.octree;

import com.google.common.collect.Maps;
import io.klinamen.joclray.geom.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FaceSet {
    private final List<Face> faces;

    public FaceSet(List<Face> faces) {
        this.faces = faces;
    }

    public FaceSet() {
        this.faces = new ArrayList<>();
    }

    public FaceSet addFace(Face face) {
        faces.add(face);
        return this;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public int size() {
        return faces.size();
    }

    public AABB getBoundingBox() {
        AABB bb = null;
        for (Face face : faces) {
            AABB bbFace = face.getBoundingBox();
            if (bb == null) {
                bb = bbFace;
            } else {
                bb = bb.merge(bbFace);
            }
        }

        return bb;
    }

    public <T> Map<T, FaceSet> partition(Function<Face, T> f) {
        HashMap<T, FaceSet> map = Maps.newHashMap();
        for (Face face : faces) {
            T partIndex = f.apply(face);
            map.computeIfAbsent(partIndex, i -> new FaceSet()).addFace(face);
        }
        return map;
    }

    @Override
    public String toString() {
        return faces.toString();
    }
}
