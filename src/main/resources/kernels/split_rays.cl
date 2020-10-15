#define BIAS 0.001f
#define DEBUG_RAY 1924795

inline float cos_phi(float n1, float n2, float4 normal, float4 rd)
{
  float cos_phi = -dot(normal, rd);
  if (n1 > n2)
  {
      float n = n1/n2;
      float sinT2 = n*n*(1.0f - cos_phi * cos_phi);
      if (sinT2 > 1.0f) {
          // Total internal reflection
          return 1.0f;
      }
      cos_phi = sqrt(1.0-sinT2);
  }

  return cos_phi;
}

/**
  Implementation from https://blog.demofox.org/2017/01/09/raytracing-reflection-refraction-fresnel-total-internal-reflection-and-beers-law/
 */
inline float4 fresnel(float n1, float n2, float cos_phi, float4 mat_kr)
{
  // Schlick aproximation
  float r0 = (n1 - n2) / (n1 + n2);
  r0 *= r0;

  float x = 1.0f - fabs(cos_phi);
  float r = r0 + (1.0f - r0) * x * x * x * x * x;

#ifdef DEBUG_RAY
  int ray = get_global_id(0);
  if(ray == DEBUG_RAY){
    printf("split_rays.fresnel: ray=%d, n1=%f, n2=%f, cos_phi=%f, r=%f\n", ray, n1, n2, cos_phi, r);
  }
#endif

 // adjust reflect multiplier for object reflectivity
  return mat_kr + (1.0f - mat_kr) * r;
}

__kernel void split_rays(
              __global const float4 *ray_origins,
              __global const float4 *ray_dirs,
              __global const float4 *ray_weights, // weight of the incident rays
              __global const float *ray_n,        // IoR of the material the incident ray is traveling through
              
              __global const float4 *hit_normals,
              __global const float *hit_distances,
              __global int *hit_map,
              
              __global const float4 *mat_kr,   // material reflectivity (indexed by element id)
              __global const float *mat_n,     // material index of refraction (indexed by element id)

              __global float4 *r_ray_origins,  // origins of the reflected rays
              __global float4 *r_ray_dirs,     // directions of the reflected rays
              __global float4 *r_ray_weights,  // weights of the reflected rays
              __global float *r_ray_n,         // IoR of the material the reflected ray is traveling through (TODO remove)
              
              __global float4 *t_ray_origins,  // origins of the transmitted rays
              __global float4 *t_ray_dirs,     // directions of the transmitted rays
              __global float4 *t_ray_weights,  // weights of the transmitted rays
              __global float *t_ray_n          // IoR of the material the transmitted ray is traveling through
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
  float rn = ray_n[ray];
  float4 rw = ray_weights[ray];

  // hit info
  float ht = hit_distances[ray];
  float4 hp = ro + ht * rd;
  float4 hn = hit_normals[ray];

  // material properties
  float4 hit_kr = mat_kr[hit_id];
  float hit_n = mat_n[hit_id];

  float cp = cos_phi(rn, hit_n, hn, rd);

  float4 r_weight = fresnel(rn, hit_n, cp, hit_kr);
  float4 t_weight = 1.0f - r_weight;

  float4 bias = cp > 0 ? BIAS * hn : -BIAS * hn;
  
  // reflection ray
  float4 rro = hp + bias;  // bias outwards to avoid self-intersections
  float4 rrd = normalize(rd - 2 * dot(rd, hn) * hn);
  
  // transmission ray
  float4 tro = hp - bias;  // bias inwards to avoid self-intersections
  float4 trd = normalize((rn/hit_n) * (rd - hn * cp) - hn * cp);

  r_ray_origins[ray] = rro;
  r_ray_dirs[ray] = rrd;
  r_ray_weights[ray] = rw * r_weight;
  r_ray_n[ray] = rn;

  t_ray_origins[ray] = tro;
  t_ray_dirs[ray] = trd;
  t_ray_weights[ray] = rw * t_weight;
  t_ray_n[ray] = hit_n;

  // reset hitMap
  // hit_map[ray] = -1;

#ifdef DEBUG_RAY
  if(ray == DEBUG_RAY){
    printf("split_rays: ray=%d, hm=%d, ro=(%f, %f, %f), rd=(%f, %f, %f), rro=(%f, %f, %f), rrd=(%f, %f, %f), rrw=(%f, %f, %f), rrn=%f, tro=(%f, %f, %f), trd=(%f, %f, %f), trw=(%f, %f, %f), trn=%f\n", 
    ray, hit_id, ro.x, ro.y, ro.x, rd.x, rd.y, rd.z,
    rro.x, rro.y, rro.z, rrd.x, rrd.y, rrd.z, r_weight.x, r_weight.y, r_weight.z,
    rn, tro.x, tro.y, tro.z, trd.x, trd.y, trd.z, t_weight.x, t_weight.y, t_weight.z,
    hit_n);
  }
#endif
}