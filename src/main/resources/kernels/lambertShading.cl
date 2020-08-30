__kernel void lambertShading(__global const float4 *normals,
                             __global const float4 *kds,
                             __global const float4 *lightPos,
                             __global const float *lightIntensity,
                             const uint nLights, const float4 e,
                             __global __write_only float4 *colors) {
  int gid = get_global_id(0);

  float4 c = kds[gid] * (float)0.0; // ambient light
  float4 n = normalize(normals[gid]);

  for (int i = 0; i < nLights; i++) {
    c += kds[gid] * lightIntensity[i] * max((float)0, dot(n, normalize(lightPos[i])));
  }

  colors[gid] = c;
}