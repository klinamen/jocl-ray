__kernel void
blinnPhongShading(__global const int *hitMap,
                  __global const float4 *hitNormals,
                  __global const float4 *kds,
                  __global const float4 *kss,
                  __global const float *phongExp,
                  __global const float4 *lightPos,
                  __global const float *lightIntensity, const uint nLights,
                  const float4 e, __global float4 *colors) {
  int gid = get_global_id(0);

  int hitElementId = hitMap[gid];
  if(hitElementId < 0){
    return;
  }

  float4 c = kds[hitElementId] * (float)0; // ambient light
  float4 n = normalize(hitNormals[gid]);

  for (int i = 0; i < nLights; i++) {
    float4 l = normalize(lightPos[i]);

    // diffusion
    c += kds[hitElementId] * lightIntensity[i] * max((float)0, dot(n, l));

    float4 h = (e + l) / length(e + l);
    c += kss[hitElementId] * lightIntensity[i] *
         pow(max((float)0, dot(n, h)), phongExp[hitElementId]);
  }

  colors[gid] = c;
}