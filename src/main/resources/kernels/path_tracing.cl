#define BIAS 0.001f
// #define DEBUG_RAY 257038
// #define DEBUG_RND 1089247

inline float random(float x) {
    float ptr = 0.0f;
    return fract(sin(x * 112.9898f) * 43758.5453f, &ptr);
}

inline float4 sample_hemisphere(float z0, float z1, float4 n, float4 u, float4 v){
  float piz0 = 2 * M_PI_F * z0;
  float z1sqrt = sqrt(z1);
  return normalize(u * cos(piz0) * z1sqrt + v * sin(piz0) * z1sqrt + n * sqrt(1.0f - z1));
}

inline float4 rl_brdf_spec(float z0, float z1, float4 n, float4 u, float4 v, float nu, float nv, float4 kd, float4 kr, float4 ki, float4 *ko){
  int q = (int)(z0 / 0.25f);
  float zphi = 1 - 4 * ((q + 1) * 0.25f - z0);

  float phi = atan(sqrt((nu + 1)/(nv + 1)) * tanpi(zphi/2)) + M_PI_2_F * q;

  float cosphi = cos(phi);
  float cos2phi = cosphi * cosphi;
  float sin2phi = 1 - cos2phi;
  float sinphi = sqrt(sin2phi);
  float nuv_term = nu * cos2phi + nv * sin2phi;

  float costheta = pow((1 - z1), 1/(nuv_term + 1));
  float sintheta = sqrt(1 - costheta * costheta);

  // h sampled from pdf_h (24.10)
  float4 h = u * cosphi * sintheta + v * sinphi * sintheta + n * costheta;

  float n_h = dot(n, h);
  float ki_h = dot(-ki, h);
  float ki_n = dot(-ki, n);
  float h_u = dot(h, u);
  float h_v = dot(h, v);

  float4 ko_s = ki + 2 * ki_h * h;
  float4 ko_d = sample_hemisphere(z0, z1, n, u, v);

  // we randomly choose the direction of the bounced ray
  *ko = z0 < 0.5f ? ko_s : ko_d;

  float ko_n = dot(n, *ko);

  // (24.12) * 8PI / sqrt((nu + 1) * (nv+1))
  float4 pdf_ko = pow(n_h, nuv_term) / ki_h;

  // Fresnel term (Schlick's approximation)
  float x = 1 - ki_h;
  float4 fresnel = kr + (1 - kr) * x*x*x*x*x;

  float4 exp = (nu * h_u * h_u + nv * h_v * h_v) / (1 - n_h * n_h);
  
  // (24.9) * 8PI / sqrt((nu + 1) * (nv+1))
  float4 rho_s = pow(n_h, exp) * fresnel / (ki_h * max(ki_n, ko_n));

  // diffuse BRDF
  float t1 = 1 - ki_n/2;
  float t2 = 1 - ko_n/2;
  float4 rho_d = ((28 * kd) / (23 * M_PI_F)) * (1 - kr) * (1 - t1*t1*t1*t1*t1) * (1 - t2*t2*t2*t2*t2);

  #ifdef DEBUG_RAY
  int ray = get_global_id(0);
  if(ray == DEBUG_RAY){
    printf("path_tracing.brdf: rho_d=(%f, %f, %f); rho_s=(%f, %f, %f); ko=(%f, %f, %f); pdf_ko=(%f, %f, %f)\n", 
      rho_d.x, rho_d.y, rho_d.z, rho_s.x, rho_s.y, rho_s.z, (*ko).x, (*ko).y, (*ko).z, pdf_ko.x, pdf_ko.y, pdf_ko.z
    );
  }
  #endif

  return rho_s * ki_n / pdf_ko + rho_d;
}

inline float4 brdf_lambertian(float z0, float z1, float4 n, float4 u, float4 v, float nu, float nv, float4 kd, float4 kr, float4 ki, float4 *ko){
  *ko = sample_hemisphere(z0, z1, n, u, v);
  return kd;
}

__kernel void path_tracing(
        __global float4 *ray_origins,
        __global float4 *ray_dirs,

        __global const float4 *hit_normals,
        __global const float *hit_dist,
        __global const int *hit_map, 

        __global const float4 *mat_kd,
        __global const float4 *mat_emission,
        
        const ulong seed0,     // random seed for z0
        const ulong seed1,     // random seed for z1

        __global float4 *radiance,
        __global float4 *color) {
  int ray = get_global_id(0);

  // no element hit -> terminate
  int hit_id = hit_map[ray];
  if (hit_id < 0) {
#if defined(DEBUG_RAY)
    if(ray == DEBUG_RAY){
      printf("path_tracing: ray=%d, MISS\n", ray);
    }
#endif

    return;
  }
  
  int rays = get_global_size(0);

  // ray
  float4 ro = ray_origins[ray];
  float4 rd = ray_dirs[ray];

  // material properties
  float4 hit_kd = mat_kd[hit_id];
  float4 hit_emission = mat_emission[hit_id];

  // hit info
  float t = hit_dist[ray];
  float4 n = hit_normals[ray];
  float4 p = ro + t * rd;

  // Orthonormal basis uvw centered at the hitpoint, s.t. w=n
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

  float4 ko;
  float4 rho;
  
  if(hit_id == 6) {
    // big sphere
    rho = rl_brdf_spec(z0, z1, n, u, v, 10,  10000, hit_kd, (float4)(0.2f, 0.2f, 0.2f, 0.f), rd, &ko);
  } else if (hit_id == 1){
    // floor
    rho = rl_brdf_spec(z0, z1, n, u, v, 10,  10000, hit_kd, (float4)(0.5f, 0.5f, 0.5f, 0.f), rd, &ko);
  } else {
    rho = brdf_lambertian(z0, z1, n, u, v, 0, 0, hit_kd, 0, rd, &ko);
  }


  // scattered ray origin and direction
  float4 bo = p + n * BIAS;
  // float4 bd = normalize(ko_d + ko_s);
  float4 bd = ko;

  // accumulate color and radiance component
  color[ray] += radiance[ray] * hit_emission;
  radiance[ray] *= rho;

  // replace ray with bounced ray
  ray_origins[ray] = bo;
  ray_dirs[ray] = bd;

#ifdef DEBUG_RAY
  if(ray == DEBUG_RAY){
    float4 c = color[ray];
    printf("path_tracing: ray=%d, hm=%d, kd=(%f, %f, %f), le=(%f, %f, %f), ro=(%f, %f, %f), rd=(%f, %f, %f), bo=(%f, %f, %f), bd=(%f, %f, %f), z0=%f, z1=%f, c=(%f, %f, %f)\n", 
    ray, hit_id, hit_kd.x, hit_kd.y, hit_kd.z, hit_emission.x, hit_emission.y, hit_emission.z, 
    ro.x, ro.y, ro.x, rd.x, rd.y, rd.z, bo.x, bo.y, bo.x, bd.x, bd.y, bd.z, z0, z1, c.x, c.y, c.z);
  }
#endif
}