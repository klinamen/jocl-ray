package io.klinamen.joclray.loaders;

import com.google.common.primitives.Ints;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.geom.TriangleMesh;
import io.klinamen.joclray.util.FloatVec4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ObjSurfaceLoader implements SurfaceLoader {
    public Surface load(InputStream inputStream) throws IOException {
        Obj obj = ObjReader.read(inputStream);

        // Prepare the Obj so that its structure is suitable for
        // rendering with OpenGL:
        // 1. Triangulate it
        // 2. Make sure that texture coordinates are not ambiguous
        // 3. Make sure that normals are not ambiguous
        // 4. Convert it to single-indexed data
//        obj = ObjUtils.convertToRenderable(obj);

        obj = ObjUtils.triangulate(obj);
        obj = ObjUtils.makeVertexIndexed(obj);

        int[] aIndices = ObjData.getFaceVertexIndicesArray(obj, 3);
        List<Integer> faceIndices = Ints.asList(aIndices);

        float[] aVertices = ObjData.getVerticesArray(obj);
        List<FloatVec4> vertices = triplesToVec4List(aVertices);

        float[] aNormals = ObjData.getNormalsArray(obj);
        List<FloatVec4> normals = triplesToVec4List(aNormals);

        return new TriangleMesh(vertices, normals, faceIndices);
    }

    private List<FloatVec4> triplesToVec4List(float[] flatten){
        List<FloatVec4> vecList = new ArrayList<>(flatten.length / 3);
        for (int i = 0; i < flatten.length; i+=3) {
            float vx = flatten[i];
            float vy = flatten[i + 1];
            float vz = flatten[i + 2];
            vecList.add(new FloatVec4(vx, vy, vz));
        }
        return vecList;
    }
}
