#define NORMAL_BIAS 1.001f

__kernel void boxIntersect(__global float4 *rayOrigins, __global float4 *rayDirections,
             __global float4 *hitNormals, __global float *hitDistances,
             __global int *hitMap, __global const int *elementIds,
             __global const float4 *vMin, __global const float4 *vMax) {
  int ray = get_global_id(0);
  int elementIndex = get_global_id(1);

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  float4 v0 = vMin[elementIndex];
  float4 v1 = vMax[elementIndex];

  float4 t0 = (v0 - ro) / rd;
  float4 t1 = (v1 - ro) / rd;

  float4 tmin = min(t0, t1);
  float4 tmax = max(t0, t1);

  if (tmax.x > tmin.y && tmax.y > tmin.x) {
    float tmin_max = max(tmin.x, max(tmin.y, tmin.z));
    float tmax_min = min(tmax.x, min(tmax.y, tmax.z));

    if (tmax.z > tmin_max && tmax_min > tmin.z) {
      float t = tmin_max;
      if(t > 0){
        // hit

        float4 hitPoint = ro + t * rd;

        // normals (https://blog.johnnovak.net/2016/10/22/the-nim-raytracer-project-part-4-calculating-box-normals/)
        float4 c = (v0 + v1) / 2;
        float4 p = hitPoint - c;
        float4 d = (v0 - v1) / 2;

        float4 n = normalize((float4)(
          (int)(p.x / fabs(d.x) * NORMAL_BIAS), 
          (int)(p.y / fabs(d.y) * NORMAL_BIAS), 
          (int)(p.z / fabs(d.z) * NORMAL_BIAS), 0)
          );

        // TODO this is unsafe, as it is accessed concurrently by multiple threads.
        if (hitMap[ray] < 0 || t < hitDistances[ray]) {
          hitMap[ray] = elementIds[elementIndex];
          hitNormals[ray] = n;
          hitDistances[ray] = t;
        }
      }
    }
  }

  // if (ray == 1072389) {
  //   float4 p = ro + hitDistances[ray] * rd;
  //   printf("Box %d: pix=%d; hm=%d; hd=%f; hn=(%f, %f, %f); p=(%f, %f, %f); ro=(%f, %f, %f); rd=(%f, %f, %f)\n",
  //          elementIndex, ray, hitMap[ray], hitDistances[ray],
  //          hitNormals[ray].x, hitNormals[ray].y, hitNormals[ray].z, p.x, p.y,
  //          p.z, ro.x, ro.y, ro.z, rd.x, rd.y, rd.z);
  // }
}