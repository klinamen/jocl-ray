#define BIAS 0.0001f
// #define DEBUG_RAY 990679
// #define DEBUG_RND 1089247

inline float random(float x) {
    float ptr = 0.0f;
    return fract(sin(x * 112.9898f) * 43758.5453f, &ptr);
}

inline float4 diffuse_sample_hem(float z0, float z1, float4 n, float4 u, float4 v){
  float phi = 2 * M_PI_F * z0;
  float costheta = sqrt(1.0f - z1);
  float sintheta = sqrt(z1);
  return normalize(u * cos(phi) * sintheta + v * sin(phi) * sintheta + n * costheta);
}

inline float4 lambertian_brdf_sample(float z0, float z1, float4 n, float4 u, float4 v, float4 *ko, float4 kd){
  *ko = diffuse_sample_hem(z0, z1, n, u, v);
  return kd;
}

__kernel void scatter_lambertian(
        __global const int *ray_queue,

        __global float4 *ray_origins,
        __global float4 *ray_dirs,

        __global const float4 *hit_normals,
        __global const float *hit_dist,
        __global const int *hit_map, 

        __global const float4 *mat_emission,
        
        const ulong seed0,     // random seed for z0
        const ulong seed1,     // random seed for z1

        __global float4 *throughput,
        __global float4 *radiance,
        
        __global const float4 *mat_kd
        ) {
  int q_index = get_global_id(0);

  int ray = ray_queue[q_index];
  int hit_id = hit_map[ray];

  // ray
  float4 ro = ray_origins[ray];
  float4 rd = ray_dirs[ray];

  // material properties
  float4 hit_emission = mat_emission[hit_id];
  float4 hit_kd = mat_kd[hit_id];

  // hit info
  float t = hit_dist[ray];
  float4 n = hit_normals[ray];
  float4 p = ro + t * rd;

  // Orthonormal basis (u,v,w) centered at the hit-point p, s.t. w=n
  float4 axis = normalize(fabs(n.y) > BIAS ? (float4)(0.0f, -n.z, n.y, 0.0f) : (float4)(n.z, 0.0f, -n.x, 0.0f));
  float4 u = normalize(cross(axis, n));
  float4 v = cross(n, u);

  // Random numbers for Monte Carlo sampling
  float z0 = random((ray ^ seed0) * 775648.367477 * (1 + fast_length(ro)));
  float z1 = random((ray ^ seed1) * 345688.99486 * (1 + fast_length(p)));

  #ifdef DEBUG_RND
  if(ray >= DEBUG_RND && ray < DEBUG_RND + 100){
    printf("path_tracing.random: ray=%d, z0=%f, z1=%f, seed0=%d, seed1=%d\n", ray, z0, z1, seed0, seed1);
  }
  #endif

  // scattered ray origin and direction
  float4 bo = p + n * BIAS;
  float4 bd;

  float4 hit_throughput = lambertian_brdf_sample(z0, z1, n, u, v, &bd, hit_kd);
  
  // accumulate radiance and throughput
  radiance[ray] += throughput[ray] * hit_emission;
  throughput[ray] *= hit_throughput;

  // replace ray with bounced ray
  ray_origins[ray] = bo;
  ray_dirs[ray] = bd;

#ifdef DEBUG_RAY
  if(ray == DEBUG_RAY){
    float4 rad = radiance[ray];
    float4 thr = throughput[ray];
    int q_size = get_global_size(0);
    printf("scatter_lambertian: ray=%d, q_index=%d, q_size=%d, hm=%d, hit_kd=(%f, %f, %f), hit_emission=(%f, %f, %f), ro=(%f, %f, %f), rd=(%f, %f, %f), bo=(%f, %f, %f), bd=(%f, %f, %f), z0=%f, z1=%f, throughput=(%f, %f, %f), radiance=(%f, %f, %f)\n", 
    ray, q_index, q_size, hit_id, hit_kd.x, hit_kd.y, hit_kd.z, hit_emission.x, hit_emission.y, hit_emission.z, 
    ro.x, ro.y, ro.x, rd.x, rd.y, rd.z, bo.x, bo.y, bo.x, bd.x, bd.y, bd.z, z0, z1, rad.x, rad.y, rad.z, thr.x, thr.y, thr.z);
  }
#endif
}