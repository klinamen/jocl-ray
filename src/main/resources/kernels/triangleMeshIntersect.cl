// #define CULLING
#define EPSILON 0.001f
#define VPF 3
// #define DEBUG_RAY 782682

__kernel void
triangleMeshIntersect(__global float4 *rayOrigins, __global float4 *rayDirections,
                  __global float4 *hitNormals, __global float *hitDistance,
                  __global int *hitMap,
                  __global const int *elementIds,
                  __global const float4 *vertices,
                  __global const int *faceIndices,
                  __global const float4 *vertexNormals) {
  int ray = get_global_id(0);
  int elementIndex = get_global_id(1);

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  // triangle
  int faceOffset = elementIndex * VPF;
  int v0Index = faceIndices[faceOffset];
  int v1Index = faceIndices[faceOffset + 1];
  int v2Index = faceIndices[faceOffset + 2];

  float4 v0 = vertices[v0Index];
  float4 v1 = vertices[v1Index];
  float4 v2 = vertices[v2Index];

  float4 e1 = v1 - v0;
  float4 e2 = v2 - v0;

  float4 pvec = cross(rd, e2);
  float det = dot(e1, pvec);

  // float det = dot(rd, n);

// #ifdef DEBUG_RAY
//   if (ray == DEBUG_RAY) {
//       printf("face=%d; v0=(%f,%f,%f); v1=(%f,%f,%f); v2=(%f,%f,%f); det=%f\n", elementIndex, v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, det);
//   }
// #endif

#ifdef CULLING
  // if the determinant is negative the triangle is backfacing
  // if the determinant is close to 0, the ray misses the triangle
  if (det < EPSILON)
    return;
#else
  // ray and triangle are parallel if det is close to 0
  if (fabs(det) < EPSILON)
    return;
#endif

  float invDet = 1 / det;

  float4 tvec = ro - v0;
  float u = dot(tvec, pvec) * invDet;
  if (u < 0 || u > 1) {
    return;
  }

  float4 qvec = cross(tvec, e1);
  float v = dot(rd, qvec) * invDet;
  if (v < 0 || u + v > 1) {
    return;
  }

  float t = dot(e2, qvec) * invDet;
  if(t < EPSILON){
    return;
  }

  // int rays = get_global_size(0);
  // int hmIndex = rays * ray + elementIndex;

  // hitMap[hmIndex] = elementIds[elementIndex];
  // hitDistances[hmIndex] = t;
  // hitNormals[hmIndex] = n;

  // TODO this is unsafe, as it is accessed concurrently by multiple threads.
  if(hitMap[ray] < 0 || t < hitDistance[ray]){
    // float4 n = normalize(cross(e1, e2));

    float4 nv0 = vertexNormals[v0Index];
    float4 nv1 = vertexNormals[v1Index];
    float4 nv2 = vertexNormals[v2Index];
    float4 n = normalize((1 - u - v) * nv0 + u * nv1 + v * nv2);
    
    hitMap[ray] = elementIds[elementIndex];
    hitNormals[ray] = n;
    hitDistance[ray] = t;
  }

#ifdef DEBUG_RAY
  if (ray == DEBUG_RAY) {
    //printf("v0=(%f,%f,%f); v1=(%f,%f,%f); v2=(%f,%f,%f)\n", v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);

    float4 p = ro + hitDistance[ray] * rd;
    printf("TriangleMesh face %d: ray=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
           elementIndex, ray, hitMap[ray], hitDistance[ray], hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, 
           p.x, p.y, p.z, ro.x, ro.y, ro.z, rd.x, rd.y, rd.z);
  }
#endif
}