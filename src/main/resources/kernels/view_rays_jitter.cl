// #define DEBUG_RAY 982134

inline float noise3D(float x, float y, float z) {
    float ptr = 0.0f;
    return fract(sin(x*112.9898f + y*179.233f + z*237.212f) * 43758.5453f, &ptr);
}

__kernel void view_rays_jitter(const float2 frameSize, const float4 e, const float fov_rad,
                              const ulong seed,   // random seed
                              const int2 samples, // per-pixel samples
                              const int2 index,   // sample index
                              __global float4 *origins,
                              __global float4 *directions) {
  int py = get_global_id(0); // image-plane row
  int px = get_global_id(1); // image-plane col

  float rnd = noise3D(px, py, seed + 11 * px + 31 * py);

  float aspectRatio = frameSize.x / frameSize.y;
  
  float x = (px + (index.x + rnd) / samples.x);
  float y = (py + (index.y + rnd) / samples.y);

  float4 rd;
  rd.x = (2 * (x / frameSize.x) - 1) * aspectRatio * tan(fov_rad / 2);
  rd.y = (1 - 2 * (y / frameSize.y)) * tan(fov_rad / 2);
  rd.z = -1;
  rd.w = 0;

  rd = normalize(rd - e);

  // ray index
  int ray = (int)frameSize.x * py + px;

  directions[ray] = rd;
  origins[ray] = e;

#ifdef DEBUG_RAY
  if(ray == DEBUG_RAY){
    printf("view_rays_jitter: ray=%d, index=(%d, %d), samples=(%d, %d), rnd=%f, xy=(%f, %f)\n", ray, index.x, index.y, samples.x, samples.y, rnd, x, y);
  }
#endif
}