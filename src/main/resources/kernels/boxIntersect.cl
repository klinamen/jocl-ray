__kernel void
boxIntersect(__global float4 *rayOrigins, __global float4 *rayDirections,
             __global float4 *hitNormals, __global float *hitDistance,
             __global int *hitMap, __global const int *elementIds,
             __global const float4 *vMin, __global const float4 *vMax) {
  int ray = get_global_id(0);
  int elementIndex = get_global_id(1);

  float4 rayOrigin = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  float4 v0 = vMin[elementIndex];
  float4 v1 = vMax[elementIndex];

  float4 t0 = (v0 - rayOrigin) / rd;
  float4 t1 = (v1 - rayOrigin) / rd;

  float4 tmin = min(t0, t1);
  float4 tmax = max(t0, t1);

  if (tmax.x > tmin.y && tmax.y > tmin.x) {
    float tmin_max = max(tmin.x, max(tmin.y, tmin.z));
    float tmax_min = min(tmax.x, min(tmax.y, tmax.z));

    if (tmax.z > tmin_max && tmax_min > tmin.z) {
      float t = tmin_max;
      if(t > 0){
        // hit

        float4 hit = rayOrigin + t * rd;

        // normals (https://blog.johnnovak.net/2016/10/22/the-nim-raytracer-project-part-4-calculating-box-normals/)
        float4 c = (v0 + v1) / 2;
        float4 p = hit - c;
        float4 d = (v0 - v1) / 2;
        float bias = 1.0001f;

        float4 n = normalize((float4)(
          (int)(p.x / fabs(d.x) * bias), 
          (int)(p.y / fabs(d.y) * bias), 
          (int)(p.z / fabs(d.z) * bias), 0)
          );

        if (hitMap[ray] < 0 || t < hitDistance[ray]) {
          hitMap[ray] = elementIds[elementIndex];
          hitNormals[ray] = n;
          hitDistance[ray] = t;
        }
      }
    }
  }

  // int pix = 1483842;
  // if (ray == pix) {
  //   float4 p = rayOrigin + hitDistance[ray] * rd;
  //   printf("Box %d: pix=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
  //          elementIndex, pix, hitMap[ray], hitDistance[ray],
  //          hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, p.x, p.y,
  //          p.z, rayOrigin.x, rayOrigin.y, rayOrigin.z, rd.x, rd.y, rd.z);
  // }
}