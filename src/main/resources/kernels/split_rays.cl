#define WORLD_IOR 1.0f
#define BIAS 0.001f
// #define DEBUG_RAY 982134


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

#ifdef DEBUG_RAY
  int ray = get_global_id(0);
  if(ray == DEBUG_RAY){
    printf("split_rays.fresnel: ray=%d, cosi=%f, ni=%f, nt=%f, r=%f\n", ray, dot(rd, hn), ni, nt, r);
  }
#endif

  return r;
}

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

__kernel void split_rays(
              __global const float4 *ray_origins,
              __global const float4 *ray_dirs,
              __global const float4 *ray_weights, // weight of the incident rays
              
              __global const float4 *hit_normals,
              __global const float *hit_distances,
              __global const int *hit_map,
              
              __global const float4 *mat_kr,   // material reflectivity (indexed by element id)
              __global const float *mat_n,     // material index of refraction (indexed by element id)

              __global float4 *r_ray_origins,  // origins of the reflected rays
              __global float4 *r_ray_dirs,     // directions of the reflected rays
              __global float4 *r_ray_weights,  // weights of the reflected rays
              
              __global float4 *t_ray_origins,  // origins of the transmitted rays
              __global float4 *t_ray_dirs,     // directions of the transmitted rays
              __global float4 *t_ray_weights   // weights of the transmitted rays
              ) {
  int ray = get_global_id(0);

  // no element hit -> terminate
  int hit_id = hit_map[ray];
  if (hit_id < 0) {
#if defined(DEBUG_RAY)
    if(ray == DEBUG_RAY){
      printf("split_rays: ray=%d, MISS\n", ray);
    }
#endif

    // zero weight to suppress effects on future generations deriving from this ray
    r_ray_weights[ray] = 0;
    t_ray_weights[ray] = 0;

    return;
  }

  // incident ray properties
  float4 ro = ray_origins[ray];
  float4 rd = ray_dirs[ray];
  float4 rw = ray_weights[ray];

  // hit info
  float ht = hit_distances[ray];
  float4 hp = ro + ht * rd;
  float4 hn = hit_normals[ray];

  // material properties
  float4 hit_kr = mat_kr[hit_id];
  float hit_n = mat_n[hit_id];

  float cosi = dot(rd, hn);
  float4 bias = BIAS * hn;

  float4 tro = 0.f;
  float4 trd = 0.f;

  float4 r_weight = hit_kr;
  float4 t_weight = 0.f;

  if(hit_n > 0){
    // hit object is refractive
    r_weight = hit_kr + (1.0f - hit_kr) * fresnel(WORLD_IOR, hit_n, rd, hn);
    t_weight = 1.0f - r_weight;
    
    if(cosi > 0){
      // invert bias if inside the object
      bias = -bias;
    }

    // transmission ray
    tro = hp - bias;  // bias to avoid self-intersections
    trd = refract(WORLD_IOR, hit_n, rd, hn);
  }

  // float4 r_weight = hit_kr + (1.0f - hit_kr) * fresnel(rn, hit_n, rd, hn);
  // float4 t_weight = 1.0f - r_weight;

  // float cosi = dot(rd, hn);
  // float4 bias = cosi > 0 ? -BIAS * hn : BIAS * hn;
  
  // reflection ray
  float4 rro = hp + bias;  // bias to avoid self-intersections
  float4 rrd = normalize(rd - 2 * cosi * hn);
  
  r_ray_origins[ray] = rro;
  r_ray_dirs[ray] = rrd;
  r_ray_weights[ray] = rw * r_weight;

  t_ray_origins[ray] = tro;
  t_ray_dirs[ray] = trd;
  t_ray_weights[ray] = rw * t_weight;

#ifdef DEBUG_RAY
  if(ray == DEBUG_RAY){
    printf("split_rays: ray=%d, hm=%d, ro=(%f, %f, %f), rd=(%f, %f, %f), rro=(%f, %f, %f), rrd=(%f, %f, %f), rrw=(%f, %f, %f), rrn=%f, tro=(%f, %f, %f), trd=(%f, %f, %f), trw=(%f, %f, %f), trn=%f\n", 
    ray, hit_id, ro.x, ro.y, ro.x, rd.x, rd.y, rd.z,
    rro.x, rro.y, rro.z, rrd.x, rrd.y, rrd.z, r_weight.x, r_weight.y, r_weight.z,
    WORLD_IOR, tro.x, tro.y, tro.z, trd.x, trd.y, trd.z, t_weight.x, t_weight.y, t_weight.z,
    hit_n);
  }
#endif
}