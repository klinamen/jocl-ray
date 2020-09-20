__kernel void sphereIntersect(__global float4 *rayOrigins,
                              __global float4 *rayDirections,
                              __global float4 *hitNormals,
                              __global float *hitDistance,
                              __global int *hitMap,
                              __global const int *elementIds,
                              __global const float4 *centers,
                              __global const float *radiuses) {
  int ray = get_global_id(0);
  int elementIndex = get_global_id(1);

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  float4 c = centers[elementIndex];
  float r = radiuses[elementIndex];

  float discr =
      pow(dot(rd, ro - c), 2) - dot(rd, rd) * (dot(ro - c, ro - c) - pow(r, 2));

  if (discr >= 0) {
    float b = -dot(rd, ro - c);
    float sqDiscr = sqrt(discr);
    float t = (b - sqDiscr) / dot(rd, rd);
    if (t < 0) {
      t = (b + sqDiscr) / dot(rd, rd);
    }

    if (t > 0) {
      // hit!

      // TODO this is unsafe, as it is accessed concurrently by multiple threads.
      if(hitMap[ray] < 0 || t < hitDistance[ray]){
        float4 p = ro + t * rd;
        float4 n = (p - c) / r;

        hitMap[ray] = elementIds[elementIndex];
        hitNormals[ray] = n;
        hitDistance[ray] = t;
      }
    }
  }

  // if (ray == 1072389) {
  //   float4 p = ro + hitDistance[ray] * rd;
  //   printf("Sphere %d: ray=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
  //          elementIndex, ray, hitMap[ray], hitDistance[ray], hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, 
  //          p.x, p.y, p.z, ro.x, ro.y, ro.z, rd.x, rd.y, rd.z);
  // }
}