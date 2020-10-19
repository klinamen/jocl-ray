// #define DEBUG_RAY 982134

inline float noise3D(float x, float y, float z) {
    float ptr = 0.0f;
    return fract(sin(x*112.9898f + y*179.233f + z*237.212f) * 43758.5453f, &ptr);
}

__kernel void view_rays_jitter(const float2 frameSize,
                              const float4 e,           // eye origin
                              const float fov_rad,      // field of view angle (rad)
                              const float aperture,     // aperture size for eye space sampling
                              const float focal_length, // focal length
                              const ulong ess_seed,     // random seed for eye space sampling
                              const ulong ips_seed,     // random seed for image plane sampling
                              const int2 ips_samples,   // per-pixel ips_samples
                              const int2 ips_index,     // image plane sample index
                              const int ess_index,     // eye space sample index
                              __global float4 *origins,
                              __global float4 *directions) {
  int py = get_global_id(0); // image-plane row
  int px = get_global_id(1); // image-plane col

  // float ips_rnd = noise3D(px, py, ips_seed + 11 * px + 31 * py);
  float ips_rnd = noise3D(px * 2347.887, py * 8873.224, ips_seed * (px ^ py));
  
  float x = (px + (ips_index.x + ips_rnd) / ips_samples.x);
  float y = (py + (ips_index.y + ips_rnd) / ips_samples.y);

  float aspectRatio = frameSize.x / frameSize.y;
  float tgfov = tan(fov_rad / 2);

  float4 rd;
  rd.x = (2 * (x / frameSize.x) - 1) * aspectRatio * tgfov;
  rd.y = (1 - 2 * (y / frameSize.y)) * tgfov;
  rd.z = -1;
  rd.w = 0;

  rd = normalize(rd - e);

  // ray ips_index
  int ray = (int)frameSize.x * py + px;

  if(aperture > 0){
    float4 p = e + focal_length * rd;
    float ess_rnd_x = noise3D(px * 3455.3377 * (ess_index ^ ess_seed), py * 7243.336 * (ess_index ^ ess_seed), ess_seed * (px ^ py));
    float ess_rnd_y = noise3D(py * 7865.4678 * (ess_index ^ ess_seed), px * 3645.9948 * (ess_index ^ ess_seed), ess_seed * (px ^ py));
  
    e.x += -aperture/2 + aperture * ess_rnd_x;
    e.y += -aperture/2 + aperture * ess_rnd_y;

    rd = normalize(p - e);

#ifdef DEBUG_RAY
    if(ray == DEBUG_RAY){
      printf("view_rays_jitter.dof: ray=%d, ess_rnd_x=%f, ess_rnd_y=%f, e.xy=(%f, %f), ess_seed=%d\n", ray, ess_rnd_x, ess_rnd_y, e.x, e.y, ess_seed);
    }
#endif
  }

  directions[ray] = rd;
  origins[ray] = e;

#ifdef DEBUG_RAY
    if(ray == DEBUG_RAY){
    printf("view_rays_jitter: ray=%d, ips_index=(%d, %d), ips_samples=(%d, %d), ips_rnd=%f, xy=(%f, %f), ips_seed=%d\n", ray, ips_index.x, ips_index.y, ips_samples.x, ips_samples.y, ips_rnd, x, y, ips_seed);
  }
#endif
}