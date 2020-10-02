// #define N 8
// #define DEPTH 1
// #define NODES_COUNT ((1 - (N << (3 * DEPTH))) / (1 - N))

#define EPSILON 0.001f
#define VPF 3
// #define DEBUG_RAY 1020967

inline bool aabbIntersect(float4 ro, float4 rd, float4 v0, float4 v1){
  float4 t0 = (v0 - ro) / rd;
  float4 t1 = (v1 - ro) / rd;

  float4 tmin = min(t0, t1);
  float4 tmax = max(t0, t1);

  float tmin_max = max(tmin.x, max(tmin.y, tmin.z));
  float tmax_min = min(tmax.x, min(tmax.y, tmax.z));

  if(tmin_max < tmax_min){
    return true;
  }

  return false;
}

inline bool triangleIntersect(float4 ro, float4 rd, float4 v0, float4 v1, float4 v2, float *tHit, float *uHit, float *vHit){
  float4 e1 = v1 - v0;
  float4 e2 = v2 - v0;

  float4 pvec = cross(rd, e2);
  float det = dot(e1, pvec);

#ifdef CULLING
  // if the determinant is negative the triangle is backfacing
  // if the determinant is close to 0, the ray misses the triangle
  if (det < EPSILON)
    return false;
#else
  // ray and triangle are parallel if det is close to 0
  if (fabs(det) < EPSILON)
    return false;
#endif

  float invDet = 1 / det;

  float4 tvec = ro - v0;
  float u = dot(tvec, pvec) * invDet;
  if (u < 0 || u > 1) {
    return false;
  }

  float4 qvec = cross(tvec, e1);
  float v = dot(rd, qvec) * invDet;
  if (v < 0 || u + v > 1) {
    return false;
  }

  float t = dot(e2, qvec) * invDet;
  if (t < EPSILON) {
    return false;
  }

  *tHit = t;
  *uHit = u;
  *vHit = v;

  return true;
}


__kernel void
octreeTriangleMeshIntersect(__global const float4 *rayOrigins, __global const float4 *rayDirections,
                  __global float4 *hitNormals, __global float *hitDistances, __global int *hitMap,
                  __global const float4 *bbMinVetrices,
                  __global const float4 *bbMaxVertices,
                  __global const int *faceSetSizes,
                  __global const int *faceSetOffsets,
                  __global const int *faceSetToElementId,
                  __global const int *faceIndices,
                  __global const float4 *vertices,
                  __global const float4 *vertexNormals,
                  int nMeshes) {
  int ray = get_global_id(0);

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  // TODO parallelize
  for(int m=0; m<nMeshes; m++){
    int meshOffset = m * NODES_COUNT;

// #ifdef DEBUG_RAY
//     if(ray == DEBUG_RAY){
//       printf("[ray=%d] Start testing mesh %d at offset %d\n", ray, m, meshOffset);
//     }
// #endif

    int stackIndex = 0;
    int nodeStack[N * DEPTH + 1];

    nodeStack[0] = 0;

    while(stackIndex > -1){
        // pop
        int nodeIndex = nodeStack[stackIndex--];

        int faceSetOffset = faceSetOffsets[nodeIndex + meshOffset];

// #ifdef DEBUG_RAY
//   if(ray == DEBUG_RAY){
//     printf("[ray=%d, mesh=%d, si=%d] POP %d, faceSetOffset=%d\n", ray, m, stackIndex + 1, nodeIndex, faceSetOffset);
//   }
// #endif

        if(faceSetOffset >= 0){
          // leaf node -> test face set

          int faceSetSize = faceSetSizes[nodeIndex + meshOffset];

          for(int i=0; i<faceSetSize; i++){
            int faceOffset = VPF * (faceSetOffset + i);

            // triangle
            int v0Index = faceIndices[faceOffset];
            int v1Index = faceIndices[faceOffset + 1];
            int v2Index = faceIndices[faceOffset + 2];

            float4 v0 = vertices[v0Index];
            float4 v1 = vertices[v1Index];
            float4 v2 = vertices[v2Index];

            float tHit, uHit, vHit;
            if(triangleIntersect(ro, rd, v0, v1, v2, &tHit, &uHit, &vHit)){
              // hit -> update hit records
              if(hitMap[ray] < 0 || tHit < hitDistances[ray]){
                // Gouraud's smoothing using vertex normals
                float4 nv0 = vertexNormals[v0Index];
                float4 nv1 = vertexNormals[v1Index];
                float4 nv2 = vertexNormals[v2Index];
                float4 n = normalize((1 - uHit - vHit) * nv0 + uHit * nv1 + vHit * nv2);
                // float4 n = normalize(cross(v1 - v0, v2 - v0));

                hitMap[ray] = faceSetToElementId[nodeIndex + meshOffset];
                hitDistances[ray] = tHit;
                hitNormals[ray] = n;
#ifdef DEBUG_RAY
                if (ray == DEBUG_RAY) {
                  float4 p = ro + hitDistances[ray] * rd;
                  printf("Mesh %d, face [%d,%d,%d], %d of %d: ray=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
                        m, v0Index, v1Index, v2Index, i, faceSetSize, ray, hitMap[ray], hitDistances[ray], hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, 
                        p.x, p.y, p.z, ro.x, ro.y, ro.z, rd.x, rd.y, rd.z);
                }
#endif

// #ifdef DEBUG_RAY
//                   if(ray == DEBUG_RAY){
//                     printf("[ray=%d, mesh=%d, si=%d] HIT: face %d:[%d,%d,%d]; hm=%d; t=%f; n=(%f, %f, %f)\n", ray, m, stackIndex, i, v0Index, v1Index, v2Index, hitMap[ray], tHit, n.x, n.y, n.z);
//                   }
// #endif
              }
            }
// #ifdef DEBUG_RAY
//             else if(ray == DEBUG_RAY) {
//               printf("[ray=%d, mesh=%d, si=%d] MISS: face %d:[%d,%d,%d]; hm=%d;\n", ray, m, stackIndex, i, v0Index, v1Index, v2Index, hitMap[ray]);
//             }
// #endif            
          }
        } else {
          // non-leaf node -> test BB

          float4 v0 = bbMinVetrices[nodeIndex + meshOffset];
          float4 v1 = bbMaxVertices[nodeIndex + meshOffset];

          if(aabbIntersect(ro, rd, v0, v1)){
            for(int i=0; i<N; i++){
              int childIndex = N * nodeIndex + 1 + i;

// #ifdef DEBUG_RAY
//   if(ray == DEBUG_RAY){
//     printf("[ray=%d, mesh=%d, si=%d] PUSH %d\n", ray, m, stackIndex, childIndex);
//   }
// #endif

              // push children
              nodeStack[++stackIndex] = childIndex;
            }
          }
        }
      }
  } 
}
