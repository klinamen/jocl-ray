__kernel void sphereIntersect2(__global float4 *rayOrigins,
                              __global float4 *rayDirections,
                              __global float4 *hitNormals,
                              __global float *hitDistance,
                              __global int *hitMap,
                              __global const int *elementIds,
                              __global const float4 *centers,
                              __global const float *radiuses) {
  int ray = get_global_id(0);
  int elementIndex = get_global_id(1);

  float4 rayOrigin = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  float4 c = centers[elementIndex];
  float r = radiuses[elementIndex];

  float discr =
      pow(dot(rd, rayOrigin - c), 2) - dot(rd, rd) * (dot(rayOrigin - c, rayOrigin - c) - pow(r, 2));

  if (discr >= 0) {
    float b = -dot(rd, rayOrigin - c);
    float sqDiscr = sqrt(discr);
    float t = (b - sqDiscr) / dot(rd, rd);
    if (t < 0) {
      t = (b + sqDiscr) / dot(rd, rd);
    }

    if (t > 0) {
      // hit!

      if(hitMap[ray] < 0 || t < hitDistance[ray]){
        float4 p = rayOrigin + t * rd;
        float4 n = (p - c) / r;

        hitMap[ray] = elementIds[elementIndex];
        hitNormals[ray] = n;
        hitDistance[ray] = t;
      }
    }
  }

  // int pix = 387072;
  // if (ray == pix) {
  //   float4 p = rayOrigin + hitDistance[ray] * rd;
  //   printf("Sphere %d: pix=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
  //          elementIndex, pix, hitMap[ray], hitDistance[ray], hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, 
  //          p.x, p.y, p.z, rayOrigin.x, rayOrigin.y, rayOrigin.z, rd.x, rd.y, rd.z);
  // }
}