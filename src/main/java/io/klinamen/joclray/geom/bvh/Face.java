package io.klinamen.joclray.geom.bvh;

import com.google.common.collect.ImmutableList;
import io.klinamen.joclray.geom.AABB;
import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.util.FloatVec4;

import java.util.List;
import java.util.stream.Collectors;

public class Face {
    private final TriangleMesh mesh;
    private final List<Integer> vertexIndices;

    public Face(TriangleMesh mesh, List<Integer> vertexIndices) {
        this.mesh = mesh;
        this.vertexIndices = vertexIndices;
    }

    public List<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public ImmutableList<FloatVec4> getVertices() {
        return vertexIndices.stream()
                .map(x -> mesh.getVertices().get(x))
                .collect(ImmutableList.toImmutableList());
    }

    public TriangleMesh getMesh() {
        return mesh;
    }

    public AABB getBoundingBox() {
        if (getVertices().size() == 0) {
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

    /**
     * https://gdbooks.gitbooks.io/3dcollisions/content/Chapter4/aabb-triangle.html
     * @param bb
     * @return
     */
    public boolean intersects(AABB bb) {
        List<FloatVec4> bbNormals = List.of(
                new FloatVec4(1.0f, 0.0f, 0.0f),
                new FloatVec4(0.0f, 1.0f, 0.0f),
                new FloatVec4(0.0f, 0.0f, 1.0f)
        );

        FloatVec4 bbCenter = bb.getCenter();
        FloatVec4 bbExt = bb.getExtents();

        List<FloatVec4> vertices = getVertices().stream()
                .map(x -> x.minus(bbCenter))
                .collect(Collectors.toList());

        List<FloatVec4> edges = List.of(
                vertices.get(1).minus(vertices.get(0)),
                vertices.get(2).minus(vertices.get(1)),
                vertices.get(0).minus(vertices.get(2))
        );


        // tests edges axes (9 axes)
        for (FloatVec4 u : bbNormals) {
            for (FloatVec4 e : edges) {
                FloatVec4 axis = u.cross(e);
                if(!satTest(vertices, bbNormals, bbExt, axis)){
                    return false;
                }
            }
        }

        // test on AABB axes (3 axes)
        for (FloatVec4 axis : bbNormals) {
            if(!satTest(vertices, bbNormals, bbExt, axis)){
                return false;
            }
        }

        // test on triangle normal
        FloatVec4 axis = edges.get(0).cross(edges.get(1));
        return satTest(vertices, bbNormals, bbExt, axis);
    }

    private boolean satTest(List<FloatVec4> vertices, List<FloatVec4> normals, FloatVec4 bbExt, FloatVec4 axis){
        float p0 = vertices.get(0).dot(axis);
        float p1 = vertices.get(1).dot(axis);
        float p2 = vertices.get(2).dot(axis);

        float r = bbExt.getX() * Math.abs(normals.get(0).dot(axis)) +
                bbExt.getY() * Math.abs(normals.get(1).dot(axis)) +
                bbExt.getZ() * Math.abs(normals.get(2).dot(axis));

        return !(Math.max(-Math.max(p0, Math.max(p1, p2)), Math.min(p0, Math.min(p1, p2))) > r);
    }

    @Override
    public String toString() {
        return vertexIndices.toString();
    }
}
