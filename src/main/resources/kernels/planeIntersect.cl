__kernel void planeIntersect(__global float4 *rayOrigins,
                              __global float4 *rayDirections,
                              __global float4 *hitNormals,
                              __global float *hitDistances,
                              __global int *hitMap,
                              __global const int *elementIds,
                              __global const float4 *planePos,
                              __global const float4 *planeNormals) {
  int ray = get_global_id(0);
  int elementIndex = get_global_id(1);

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  float4 p = planePos[elementIndex];
  float4 n = normalize(planeNormals[elementIndex]);

  float den = dot(rd, n);

  if (fabs(den) > 0.001f) {
    float t = dot(p - ro, n) / den;

    if (t >= 0) {
      // hit!

      // TODO this is unsafe, as it is accessed concurrently by multiple threads.
      if (hitMap[ray] < 0 || t < hitDistances[ray]) {
        hitMap[ray] = elementIds[elementIndex];
        hitNormals[ray] = n;
        hitDistances[ray] = t;
      }
    }
  }

  // if (ray == 1072389) {
  //   float4 p = ro + hitDistances[ray] * rd;
  //   printf("Plane %d: pix=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
  //          elementIndex, ray, hitMap[ray], hitDistances[ray], hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, 
  //          p.x, p.y, p.z, ro.x, ro.y, ro.z, rd.x, rd.y, rd.z);
  // }
}