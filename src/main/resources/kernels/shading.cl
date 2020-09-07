__kernel void shading(__global float4 *rayOrigins,
                      __global float4 *rayDirections,
                      __global float4 *hitNormals,
                      __global float *hitDistances,
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

  // no element hit -> terminate
  int hitElementId = hitMap[ray];
  if (hitElementId < 0) {
    return;
  }
  
  int rays = get_global_size(0);

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  // hit info
  float t = hitDistances[ray];
  float4 p = ro + t * rd;
  float4 n = normalize(hitNormals[ray]);

  // Ambient component
  float4 c = kd[hitElementId] * aLightIntensity;

  // TODO parallelize as for intersections
  for (int i = 0; i < nLights; i++) {
    float intensity = lightIntensityMap[i * rays + ray];
    float4 l = normalize(lightPos[i] - p);

    // TODO hacky
    float angle = lightAngle[i];
    if(angle > 0){
      // Spotlight
      float4 ld = normalize(lightDirection[i]);
      float cosphi = cospi(acos(dot(-l, ld)) / angle);
      intensity = intensity * (cosphi < 0 ? 0 : pow(cosphi, 1));
    }

    // Lambertian component
    c += kd[hitElementId] * intensity * max((float)0, dot(n, l));

    // Blinn-Phong component
    float4 h = normalize(-rd + l);
    c += ks[hitElementId] * intensity *
         pow(max((float)0, dot(n, h)), phongExp[hitElementId]);
  }

  // Final color. For primary rays, krPrev[ray] is initialized to 1.
  colors[ray] += krPrev[ray] * c;

  // bounce rays!
  // TODO move to another kernel
  float4 rrd = normalize(rd - 2 * dot(rd, n) * n);
  float4 rro = p + 0.001f * n;  // bias to avoid self-intersections

  // update rays with reflected ones
  rayOrigins[ray] = rro;
  rayDirections[ray] = rrd;

  // accumulate kr of the hit object for this ray
  krPrev[ray] *= kr[hitElementId];
}