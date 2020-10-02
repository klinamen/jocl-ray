package io.klinamen.joclray.geom;

import io.klinamen.joclray.geom.bvh.Face;
import io.klinamen.joclray.geom.bvh.FaceSet;
import io.klinamen.joclray.transformations.Transformation;
import io.klinamen.joclray.util.FloatVec4;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TriangleMesh extends Surface {
    private static final int VPF = 3;

    private List<FloatVec4> vertices;
    private List<FloatVec4> normals;
    private List<Integer> faceIndices;

    public TriangleMesh(List<FloatVec4> vertices, List<FloatVec4> normals, List<Integer> faceIndices) {
        this.vertices = vertices;
        this.normals = normals;
        this.faceIndices = faceIndices;
    }

    public TriangleMesh() {
        this.vertices = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.faceIndices = new ArrayList<>();
    }

    public TriangleMesh addVertex(FloatVec4 v){
        vertices.add(v);
        return this;
    }

    public TriangleMesh addVertex(float x, float y, float z){
        vertices.add(new FloatVec4(x, y, z));
        return this;
    }

    public TriangleMesh addFace(int v0, int v1, int v2){
        if(v0 < 0 || v0 > vertices.size()){
            throw new IllegalArgumentException("Invalid vertex v0");
        }

        if(v1 < 0 || v1 > vertices.size()){
            throw new IllegalArgumentException("Invalid vertex v1");
        }

        if(v2 < 0 || v2 > vertices.size()){
            throw new IllegalArgumentException("Invalid vertex v2");
        }

        faceIndices.add(v0);
        faceIndices.add(v1);
        faceIndices.add(v2);

        return this;
    }

    public List<FloatVec4> getVertices() {
        return vertices;
    }

    public List<FloatVec4> getNormals() {
        return normals;
    }

    public List<Integer> getFaceIndices() {
        return faceIndices;
    }

    public int getTotalFaces(){
        return faceIndices.size() / VPF;
    }

    public FaceSet getFaceSet(){
        FaceSet faceSet = new FaceSet();
        List<Integer> indices = getFaceIndices();
        for (int j = 0; j < indices.size(); j+=VPF) {
            List<Integer> vIndices = indices.subList(j, j + VPF);
            faceSet.addFace(new Face(this, vIndices));
        }
        return faceSet;
    }

    public AABB getBoundingBox(){
        if(getVertices().size() == 0) {
            throw new IllegalStateException("Unable to find bounding box. No vertices are defined.");
        }

        FloatVec4 min = null;
        FloatVec4 max = null;
        for (FloatVec4 v : getVertices()) {
            min = v.min(min);
            max = v.max(max);
        }

        return new AABB(min, max);
    }

    public TriangleMesh transform(Transformation t){
        vertices = vertices.stream()
                .map(t::apply)
                .collect(Collectors.toList());

        Transformation tNormal = t.invert().transpose();
        normals = normals.stream()
                .map(tNormal::apply)
                .collect(Collectors.toList());

        return this;
    }
}

