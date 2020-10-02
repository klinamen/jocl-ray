// #define CULLING
#define EPSILON 0.001f
#define VPF 3
#define DEBUG_RAY 646036

__kernel void triangleFaceSetIntersect(
    __global const float4 *rayOrigins, __global const float4 *rayDirections,
    __global float4 *hitNormals, __global float *hitDistance, __global int *hitMap,
    __global const int *rayToFaceSet,
    __global const int *rayToFaceSetSize,
    __global const int *elementIds, __global const float4 *vertices,
    __global const int *faceIndices, __global const float4 *vertexNormals) {
  int ray = get_global_id(0);

  int faceSetOffset = rayToFaceSet[ray];
  if (faceSetOffset < 0) {
    return;
  }

  int nFaces = rayToFaceSetSize[ray];

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  int hitElementId = -1;
  float tHit = 0;
  float uHit = 0;
  float vHit = 0;
  int v0IndexHit = 0;
  int v1IndexHit = 0;
  int v2IndexHit = 0;

  for (int i = 0; i < nFaces; i++) {
    // triangle
    int v0Index = faceIndices[faceSetOffset + i * VPF];
    int v1Index = faceIndices[faceSetOffset + i * VPF + 1];
    int v2Index = faceIndices[faceSetOffset + i * VPF + 2];

    float4 v0 = vertices[v0Index];
    float4 v1 = vertices[v1Index];
    float4 v2 = vertices[v2Index];

    int elementIndex = faceSetOffset + i;
    int elementId = elementIds[elementIndex];

#ifdef DEBUG_RAY
    if (ray == DEBUG_RAY) {
      printf("testing fso=%d, i=%d/%d, el=%d: %d:(%f, %f, %f), %d:(%f, %f, %f), %d:(%f, %f, %f)\n", 
        faceSetOffset, i, nFaces - 1, elementId, v0Index, v0.x, v0.y, v0.z, v1Index, v1.x, v1.y, v1.z, v2Index, v2.x, v2.y, v2.z);
    }
#endif

    float4 e1 = v1 - v0;
    float4 e2 = v2 - v0;

    float4 pvec = cross(rd, e2);
    float det = dot(e1, pvec);

#ifdef CULLING
    // if the determinant is negative the triangle is backfacing
    // if the determinant is close to 0, the ray misses the triangle
    if (det < EPSILON)
      continue;
#else
    // ray and triangle are parallel if det is close to 0
    if (fabs(det) < EPSILON)
      continue;
#endif

    float invDet = 1 / det;

    float4 tvec = ro - v0;
    float u = dot(tvec, pvec) * invDet;
    if (u < 0 || u > 1) {
      continue;
    }

    float4 qvec = cross(tvec, e1);
    float v = dot(rd, qvec) * invDet;
    if (v < 0 || u + v > 1) {
      continue;
    }

    float t = dot(e2, qvec) * invDet;
    if (t < EPSILON) {
      continue;
    }

    if (hitElementId < 0 || t < tHit) {
      hitElementId = elementId;
      tHit = t;
      v0IndexHit = v0Index;
      v1IndexHit = v1Index;
      v2IndexHit = v2Index;
      uHit = u;
      vHit = v;
    }
  }

  if (hitElementId >= 0) {
    float4 nv0 = vertexNormals[v0IndexHit];
    float4 nv1 = vertexNormals[v1IndexHit];
    float4 nv2 = vertexNormals[v2IndexHit];
    float4 n = normalize((1 - uHit - vHit) * nv0 + uHit * nv1 + vHit * nv2);

    hitMap[ray] = hitElementId;
    hitNormals[ray] = n;
    hitDistance[ray] = tHit;
  }

#ifdef DEBUG_RAY
  if (ray == DEBUG_RAY) {
    float4 p = ro + hitDistance[ray] * rd;
    printf("AccTriMesh: ray=%d; faceSetOffset=%d; nFaces=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
           ray, faceSetOffset, nFaces, hitMap[ray], hitDistance[ray], hitNormals[ray].x,
           hitNormals[ray].y, hitNormals[ray].z, p.x, p.y, p.z, ro.x, ro.y,
           ro.z, rd.x, rd.y, rd.z);
  }
#endif
}