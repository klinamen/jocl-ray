// #define DEBUG_RAY 1291857
// #define DEBUG_LIGHTS

__kernel void dl_blinn_phong(
        __global const float4 *ray_origins,
        __global const float4 *ray_dirs,
        __global const float4 *ray_weights,

        __global const float4 *hit_normals,
        __global const float *hit_dist,
        __global const int *hit_map, 

        __global const float4 *mat_kd,
        __global const float4 *mat_ks,
        __global const float *mat_ph,

        const uint n_lights,
        const float amb_light_int,
        __global const float4 *light_pos,
        __global const float *light_int_map, 
        __global const float4 *light_dirs,
        __global const float *light_angle, 
        __global const float *light_fallout, 

        __global float4 *colors) {
  int ray = get_global_id(0);

  // no element hit -> terminate
  int hit_id = hit_map[ray];
  if (hit_id < 0) {
#if defined(DEBUG_RAY)
    if(ray == DEBUG_RAY){
      printf("dl_blinn_phong: ray=%d, MISS\n", ray);
    }
#endif

    return;
  }
  
  int rays = get_global_size(0);

  // ray
  float4 ro = ray_origins[ray];
  float4 rd = ray_dirs[ray];
  float4 rw = ray_weights[ray];

  // material properties
  float4 hit_kd = mat_kd[hit_id];
  float4 hit_ks = mat_ks[hit_id];
  float hit_ph = mat_ph[hit_id];

  // hit info
  float t = hit_dist[ray];
  float4 p = ro + t * rd;
  float4 n = hit_normals[ray];

  // Ambient component
  float4 c = hit_kd * amb_light_int;

  // TODO parallelize as for intersections
  for (int i = 0; i < n_lights; i++) {
    float intensity = light_int_map[i * rays + ray];
    float4 l = normalize(light_pos[i] - p);

#if defined(DEBUG_RAY) && defined(DEBUG_LIGHTS)
    if(ray == DEBUG_RAY){
      printf("dl_blinn_phong: ray=%d, light=%d, int=%f\n", ray, i, intensity);
    }
#endif

    // TODO hacky
    float angle = light_angle[i];
    if(angle > 0){
      // Spotlight
      float fallout = light_fallout[i];
      float4 ld = normalize(light_dirs[i]);
      float b = acos(dot(-l, ld));
      float kAtt = (b <= angle/2) * pow(cospi(b/angle), fallout);
      intensity = intensity * kAtt;

#if defined(DEBUG_RAY) && defined(DEBUG_LIGHTS)
      if(ray == DEBUG_RAY){
        printf("dl_blinn_phong: ray=%d, Spotlight: kAtt=%f, int=%f\n", ray, kAtt, intensity);
      }
#endif
    }

    // Lambertian component
    c += hit_kd * intensity * max((float)0, dot(n, l));

    // Blinn-Phong component
    float4 h = normalize(-rd + l);
    c += hit_ks * intensity * pow(max((float)0, dot(n, h)), hit_ph);
  }
  
  // Final color contribution, weighted by ray weight rw.
  float4 cc = rw * c;

  colors[ray] += cc;

#ifdef DEBUG_RAY
  if(ray == DEBUG_RAY){
    printf("dl_blinn_phong: ray=%d, hm=%d, ro=(%f, %f, %f), rd=(%f, %f, %f), rw=(%f, %f, %f), cc=(%f, %f, %f), c=(%f, %f, %f)\n", 
    ray, hit_id, ro.x, ro.y, ro.x, rd.x, rd.y, rd.z, rw.x, rw.y, rw.z,
    cc.x, cc.y, cc.z, colors[ray].x, colors[ray].y, colors[ray].z);
  }
#endif
}