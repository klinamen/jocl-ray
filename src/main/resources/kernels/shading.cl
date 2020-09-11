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
      float b = acos(dot(-l, ld));
      float kAtt = (b > angle/2 ? 0 : pow(cospi(b/angle), 1.0f));      
      intensity = intensity * kAtt;

      // if(i==1 && ray % 2 == 0){
      //   printf("pix=%d, light_i=%d, b=%f, kAtt=%f\n", ray, i, b, kAtt);
      // }
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
  hitMap[ray] = -1; // resets hitMap for each bounce

  // accumulate kr of the hit object for this ray
  // c(i) = bp(i) + kr(i) * c(i-1) = bp(i) + kr(i) * (bp(i-1) + kr(i-1) * c(i-2)) = ...
  // = bp(0) + kr(1) * bp(1) + kr(1) * kr(2) * bp(2) + ...
  krPrev[ray] *= kr[hitElementId];

  // if(ray == 1072389){
  //   float4 cc = krPrev[ray] * c;
  //   printf("Shading: ray=%d, hm=%d, ro=(%f, %f, %f), rd=(%f, %f, %f), rro=(%f, %f, %f), rrd=(%f, %f, %f), cc=(%f, %f, %f), c=(%f, %f, %f)\n", 
  //   ray, hitElementId, ro.x, ro.y, ro.x, rd.x, rd.y, rd.z, rro.x, rro.y, rro.z, rrd.x, rrd.y, rrd.z,
  //   cc.x, cc.y, cc.z, colors[ray].x, colors[ray].y, colors[ray].z);
  // }
}