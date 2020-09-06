__kernel void planeIntersect2(__global float4 *rayOrigins,
                              __global float4 *rayDirections,
                              __global float4 *hitNormals,
                              __global float *hitDistance, __global int *hitMap,
                              __global const int *elementIds,
                              __global const float4 *planePos,
                              __global const float4 *planeNormals) {
  int ray = get_global_id(0);
  int elementIndex = get_global_id(1);

  float4 rayOrigin = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  float4 p = planePos[elementIndex];
  float4 n = normalize(planeNormals[elementIndex]);

  float den = dot(rd, n);

  if (fabs(den) > 0.001f) {
    float t = dot(p - rayOrigin, n) / den;

    if (t >= 0) {
      // hit!

      if (hitMap[ray] < 0 || t < hitDistance[ray]) {
        hitMap[ray] = elementIds[elementIndex];
        hitNormals[ray] = n;
        hitDistance[ray] = t;
      }
    }
  }

  // int pix = 387072;
  // if (ray == pix) {
  //   float4 p = rayOrigin + hitDistance[ray] * rd;
  //   printf("Plane %d: pix=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
  //          elementIndex, pix, hitMap[ray], hitDistance[ray], hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, 
  //          p.x, p.y, p.z, rayOrigin.x, rayOrigin.y, rayOrigin.z, rd.x, rd.y, rd.z);
  // }
}