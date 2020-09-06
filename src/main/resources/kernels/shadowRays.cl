__kernel void
shadowRays(__global float4 *rayOrigin, __global float4 *rayDirections,
           __global float4 *hitNormals, __global float *hitDistance,
           __global int *hitMap, __global float4 *lightPos,
           __global float4 *shadowRayOrigin, __global float4 *shadowRayDir) {

  int ray = get_global_id(0);

  int hitElementId = hitMap[ray];
  if (hitElementId < 0) {
    return;
  }

  int lightIndex = get_global_id(1);

  // ray
  float4 e = rayOrigin[ray];
  float4 rd = rayDirections[ray];

  // hit
  float t = hitDistance[ray];
  float4 n = hitNormals[ray];
  float4 p = e + t * rd;

  // shadow ray
  int rays = get_global_size(0);
  int shadowIndex = lightIndex * rays + ray;

  float4 srd = normalize(lightPos[lightIndex] - p);
  float4 sro = p + 0.001f * n;

  shadowRayOrigin[shadowIndex] = sro;
  shadowRayDir[shadowIndex] = srd;

  // int pix = 387072;
  // if (ray == pix) {
  //   printf("Shadow: pix=%d; l=%d; vro=(%f, %f, %f); vrd=(%f, %f, %f); sro=(%f, %f, %f); srd=(%f, %f, %f)\n",
  //          pix, lightIndex, e.x, e.y, e.z, rd.x, rd.y, rd.z, sro.x, sro.y, sro.z, srd.x, srd.y, srd.z);
  // }
}
