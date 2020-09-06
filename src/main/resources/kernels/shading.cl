__kernel void shading(__global float4 *rayOrigin,
                      __global float4 *rayDirections,
                      __global float4 *hitNormals,
                      __global float *hitDistance,
                      __global int *hitMap, 
                      const float aLightIntensity,
                      __global const float4 *kd, __global const float4 *ks,
                      __global const float4 *kr, __global const float *phongExp,
                      __global float4 *krPrev, __global const float4 *lightPos,
                      __global const float *lightIntensityMap, 
                      __global const float4 *lightDirection,
                      __global const float *lightAngle, 
                      const uint nLights,
                      __global float4 *colors) {
  int ray = get_global_id(0);

  int hitElementId = hitMap[ray];
  if (hitElementId < 0) {
    return;
  }
  
  int rays = get_global_size(0);

  // ray
  float4 e = rayOrigin[ray];
  float4 rd = rayDirections[ray];

  // hit
  float t = hitDistance[ray];
  float4 p = e + t * rd;
  float4 n = normalize(hitNormals[ray]);

  // v points towards e
  float4 v = -rd;

  // Ambient component
  float4 c = kd[hitElementId] * aLightIntensity;

  for (int i = 0; i < nLights; i++) {
    float intensity = lightIntensityMap[i * rays + ray];
    float4 l = normalize(lightPos[i] - p);

    // TODO hacky
    float angle = lightAngle[i];
    if(angle > 0){
      // spotlight
      float4 ld = normalize(lightDirection[i]);
      float cosphi = cospi(acos(dot(-l, ld)) / angle);
      intensity = intensity * (cosphi < 0 ? 0 : pow(cosphi, 1));
    }

    // Lambert component
    c += kd[hitElementId] * intensity * max((float)0, dot(n, l));

    // Blinn-Phong component
    float4 h = normalize(v + l);
    c += ks[hitElementId] * intensity *
         pow(max((float)0, dot(n, h)), phongExp[hitElementId]);
  }

  // int pix = 1024 * (1024/2) + (1280/2);
  // if (ray == pix) {
  //   printf("pix=%d; e=(%f, %f, %f); rd=(%f, %f, %f); c=(%f, %f, %f)\n", pix, e.x, e.y, e.z, rd.x, rd.y, rd.z, c.x, c.y, c.z);
  // }

  // Final color. For primary rays, krPrev[ray] = 1.
  colors[ray] += krPrev[ray] * c;

  // bounce

  // float4 rrd = normalize(-v + 2 * dot(v, n) * n);
  float4 rrd = normalize(-v - 2 * dot(-v, n) * n);
  float4 rro = p + 0.001f * n;

  rayOrigin[ray] = rro;
  rayDirections[ray] = rrd;

  // accumulate kr of the hit object for this ray
  krPrev[ray] *= kr[hitElementId];
}