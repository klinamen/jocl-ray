#define WORLD_IOR 1.0f
#define BIAS 0.0001f
// #define DEBUG_RAY 1348076

inline float random(float x) {
    float ptr = 0.0f;
    return fract(sin(x * 112.9898f) * 43758.5453f, &ptr);
}

/**
 * Implementation adapted from https://www.scratchapixel.com/lessons/3d-basic-rendering/introduction-to-shading/reflection-refraction-fresnel 
 */
inline float fresnel(float ni, float nt, float4 rd, float4 hn) 
{ 
  float cosi = dot(rd, hn);
  if (cosi > 0) {
    float tmp = nt;
    nt = ni;
    ni = tmp;
  }

  // Compute sint using Snell's law
  float sint = ni / nt * sqrt(max(0.f, 1 - cosi * cosi));
  if (sint >= 1.0f) { 
    // Total internal reflection
    return 1.0f;
  }
  
  float cost = sqrt(max(0.f, 1.0f - sint * sint)); 
  cosi = fabs(cosi); 
  float Rs = ((nt * cosi) - (ni * cost)) / ((nt * cosi) + (ni * cost)); 
  float Rp = ((ni * cosi) - (nt * cost)) / ((ni * cosi) + (nt * cost)); 
  float r = (Rs * Rs + Rp * Rp) / 2;

  return r;
}

/**
 * Implementation adapted from https://www.scratchapixel.com/lessons/3d-basic-rendering/introduction-to-shading/reflection-refraction-fresnel 
 */
inline float4 refract(float ni, float nt, float4 rd, float4 hn) 
{ 
    float cosi = dot(rd, hn); 
    float4 norm = hn; 
    if (cosi < 0) { 
      cosi = -cosi; 
    } else { 
      float tmp = nt;
      nt = ni;
      ni = tmp;
      norm = -norm; 
    } 

    float n = ni / nt; 
    float k = 1 - n * n * (1 - cosi * cosi); 
    return k < 0 ? 0 : n * rd + (n * cosi - sqrt(k)) * norm;
} 

inline float4 glass_bxdf_sample(int ray, float z0, float4 n, float4 u, float4 v, float4 ki, float4 *ko, float4 *bias, float ni, float nt, float4 kt, float4 kr){
  *bias = n * BIAS;
  float costheta = dot(-ki, n);

  float f = fresnel(ni, nt, -ki, n);
  if(z0 < f){
    // specular reflection
    // *ko = (float4)(-ki.x, -ki.y, ki.z, 0);
    *ko = normalize(-ki - 2 * costheta * n);
    return kr * f;
  } else {
    // specular transmission
    *ko = refract(ni, nt, -ki, n);

    *bias = - *bias;

    float etaT = nt;
    float etaI = ni;
    if(costheta > 0){
      // incident ray is inside -> invert bias
      *bias = - *bias;
      etaT = ni;
      etaI = nt;
    }
    
    float4 r = kt * (1 - f) * ((etaI * etaI) / (etaT * etaT));

    #ifdef DEBUG_RAY
    if(ray == DEBUG_RAY){
      printf("ray=%d, f=%f, costheta=%f, n=(%f, %f, %f) r=(%f,%f,%f)\n", ray, f, costheta, n.x, n.y, n.z, r.x, r.y, r.z);
    }
    #endif

    return kt * (1 - f) * ((etaI * etaI) / (etaT * etaT));
  }
}

__kernel void scatter_glass(
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
        
        __global const float4 *mat_kt,
        __global const float4 *mat_kr,
        __global const float *mat_ior
        ) {
  int q_index = get_global_id(0);

  int ray = ray_queue[q_index];
  int hit_id = hit_map[ray];

  // ray
  float4 ro = ray_origins[ray];
  float4 rd = ray_dirs[ray];

  // material properties
  float4 hit_emission = mat_emission[hit_id];
  float4 hit_kt = mat_kt[hit_id];
  float4 hit_kr = mat_kr[hit_id];
  float hit_ior = mat_ior[hit_id];

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

  // scattered ray origin and direction
  float4 bias;
  float4 bd;

  float4 hit_throughput = glass_bxdf_sample(ray, z0, n, u, v, -rd, &bd, &bias, WORLD_IOR, hit_ior, hit_kt, hit_kr);

  float4 bo = p + bias;

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
    printf("scatter_glass: ray=%d, q_index=%d, q_size=%d, hm=%d, hit_kt=(%f, %f, %f), hit_emission=(%f, %f, %f), ro=(%f, %f, %f), rd=(%f, %f, %f), bo=(%f, %f, %f), bd=(%f, %f, %f), z0=%f, z1=%f, h_thr=(%f, %f, %f), throughput=(%f, %f, %f), radiance=(%f, %f, %f)\n", 
      ray, q_index, q_size, hit_id, hit_kt.x, hit_kt.y, hit_kt.z, hit_emission.x, hit_emission.y, hit_emission.z, 
      ro.x, ro.y, ro.x, rd.x, rd.y, rd.z, bo.x, bo.y, bo.x, bd.x, bd.y, bd.z, z0, z1, hit_throughput.x, hit_throughput.y, hit_throughput.z,  thr.x, thr.y, thr.z, rad.x, rad.y, rad.z);
  }
#endif
}