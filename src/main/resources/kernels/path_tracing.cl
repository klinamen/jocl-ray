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

inline float4 rl_spec_sample_hem(float z0, float z1, float4 n, float4 u, float4 v, float nu, float nv, float4 ki, float4 *pdf, float4 *h){
  int q = (int)(z0 / 0.25f);
  float z0_rescaled = 1 - 4 * ((q + 1) * 0.25f - z0);

  float phi = atan(sqrt((nu + 1)/(nv + 1)) * tanpi(z0_rescaled/2)) + M_PI_2_F * q;

  float cosphi = cos(phi);
  float cos2phi = cosphi * cosphi;
  float sin2phi = 1 - cos2phi;
  float sinphi = sqrt(sin2phi);

  float nuv_term = nu * cos2phi + nv * sin2phi;

  float costheta = pow((1 - z1), 1/(nuv_term + 1));
  float sintheta = sqrt(1 - costheta * costheta);

  // h sampled from p_h (24.10)
  *h = u * cosphi * sintheta + v * sinphi * sintheta + n * costheta;

  float n_h = dot(n, *h);
  float ki_h = dot(ki, *h);

  // (24.12) * 8PI / sqrt((nu + 1) * (nv + 1))
  *pdf = pow(n_h, nuv_term) / ki_h;

  #ifdef DEBUG_RAY
  int ray = get_global_id(0);
  if(ray == DEBUG_RAY){
    printf("path_tracing.rl_spec_sample_hem: h=(%f, %f, %f); ki_h=%f\n", 
      (*h).x, (*h).y, (*h).z, ki_h
    );
  }
  #endif

  return -ki + 2 * ki_h * (*h);
}

// TODO avoid passing h as parameter
inline float4 rl_brdf_spec_rho(float4 n, float4 u, float4 v, float4 ki, float4 ko, float4 h, float nu, float nv, float4 kr){
  // float4 h = normalize(ki + ko);

  float n_h = dot(n, h);
  float ki_h = dot(ki, h);
  float ki_n = dot(ki, n);
  float h_u = dot(h, u);
  float h_v = dot(h, v);
  float ko_n = dot(n, ko);

  // Fresnel term (Schlick's approximation)
  float x = 1 - ki_h;
  float4 fresnel = kr + (1 - kr) * x*x*x*x*x;

  float4 exp = (nu * h_u*h_u + nv * h_v*h_v) / (1 - n_h*n_h);
  
  // (24.9) * 8PI / sqrt((nu + 1) * (nv + 1))
  float4 rho = pow(n_h, exp) * fresnel / (ki_h * max(ki_n, ko_n));

  #ifdef DEBUG_RAY
  int ray = get_global_id(0);
  if(ray == DEBUG_RAY){
    printf("path_tracing.rl_brdf_spec_rho: h=(%f, %f, %f); ki_h=%f; ki_n=%f; ko_n=%f\n", 
      h.x, h.y,h.z, ki_h, ki_n, ko_n
    );
  }
  #endif

  return rho;
}

inline float4 rl_brdf_diff_rho(float4 n, float4 ki, float4 ko, float4 kd, float4 kr){
  float t1 = 1 - dot(ki, n)/2;
  float t2 = 1 - dot(ko, n)/2;
  float4 rho = ((28 * kd) / (23 * M_PI_F)) * (1 - kr) * (1 - t1*t1*t1*t1*t1) * (1 - t2*t2*t2*t2*t2);
  return rho;
}

inline float4 rl_brdf_sample(float z0, float z1, float4 n, float4 u, float4 v, float4 ki, float4 *ko, float nu, float nv, float4 kd, float4 kr){
  float4 pdf_s, h;
  float4 ko_s = rl_spec_sample_hem(z0, z1, n, u, v, nu, nv, ki, &pdf_s, &h);
  float4 ko_d = diffuse_sample_hem(z0, z1, n, u, v);

  float4 rho_s = rl_brdf_spec_rho(n, u, v, ki, ko_s, h, nu, nv, kr);
  float4 rho_d = rl_brdf_diff_rho(n, ki, ko_d, kd, kr);

  #ifdef DEBUG_RAY
  int ray = get_global_id(0);
  if(ray == DEBUG_RAY){
    printf("path_tracing.rl_brdf_sample: rho_d=(%f, %f, %f); rho_s=(%f, %f, %f); pdf_s=(%f, %f, %f)\n", 
      rho_d.x, rho_d.y, rho_d.z, rho_s.x, rho_s.y, rho_s.z, pdf_s.x, pdf_s.y, pdf_s.z
    );
  }
  #endif

  // random selection of ko, between the specular and diffuse samples
  *ko = z0 < 0.5f ? ko_s : ko_d;

  return rho_s * dot(ki, n) / pdf_s + rho_d;
}

inline float4 lambertian_brdf_sample(float z0, float z1, float4 n, float4 u, float4 v, float4 *ko, float4 kd){
  *ko = diffuse_sample_hem(z0, z1, n, u, v);
  return kd;
}

inline float3 mod289(float3 x) {
    return x - floor(x * (1.0f / 289.0f)) * 289.0f;
}

inline float4 mod289_4(float4 x) {
    return x - floor(x * (1.0f / 289.0f)) * 289.0f;
}

inline float4 permute(float4 x) {
    return mod289_4(((x*34.0f)+1.0f)*x);
}

inline float4 taylorInvSqrt(float4 r) {
    return 1.79284291400159f - 0.85373472095314f * r;
}

inline float snoise(float3 v) {
    const float2  C = (float2)(1.0f/6.0f, 1.0f/3.0f) ;
    const float4  D = (float4)(0.0f, 0.5f, 1.0f, 2.0f);

    // First corner
    float3 i  = floor(v + dot(v, C.yyy) );
    float3 x0 =   v - i + dot(i, C.xxx) ;

    // Other corners
    float3 g = step(x0.yzx, x0.xyz);
    float3 l = 1.0f - g;
    float3 i1 = min( g.xyz, l.zxy );
    float3 i2 = max( g.xyz, l.zxy );

    //   x0 = x0 - 0.0 + 0.0 * C.xxx;
    //   x1 = x0 - i1  + 1.0 * C.xxx;
    //   x2 = x0 - i2  + 2.0 * C.xxx;
    //   x3 = x0 - 1.0 + 3.0 * C.xxx;
    float3 x1 = x0 - i1 + C.xxx;
    float3 x2 = x0 - i2 + C.yyy; // 2.0*C.x = 1/3 = C.y
    float3 x3 = x0 - D.yyy;      // -1.0+3.0*C.x = -0.5 = -D.y

    // Permutations
    i = mod289(i);
    float4 p = permute( permute( permute(
                    i.z + (float4)(0.0f, i1.z, i2.z, 1.0f ))
                + i.y + (float4)(0.0f, i1.y, i2.y, 1.0f ))
            + i.x + (float4)(0.0f, i1.x, i2.x, 1.0f ));

    // Gradients: 7x7 points over a square, mapped onto an octahedron.
    // The ring size 17*17 = 289 is close to a multiple of 49 (49*6 = 294)
    float n_ = 0.142857142857f; // 1.0/7.0
    float3  ns = n_ * D.wyz - D.xzx;

    float4 j = p - 49.0f * floor(p * ns.z * ns.z);  //  mod(p,7*7)

    float4 x_ = floor(j * ns.z);
    float4 y_ = floor(j - 7.0f * x_ );    // mod(j,N)

    float4 x = x_ *ns.x + ns.yyyy;
    float4 y = y_ *ns.x + ns.yyyy;
    float4 h = 1.0f - fabs(x) - fabs(y);

    float4 b0 = (float4)( x.xy, y.xy );
    float4 b1 = (float4)( x.zw, y.zw );

    float4 s0 = floor(b0)*2.0f + 1.0f;
    float4 s1 = floor(b1)*2.0f + 1.0f;
    float4 sh = -step(h, (float4)(0.0f));

    float4 a0 = b0.xzyw + s0.xzyw*sh.xxyy ;
    float4 a1 = b1.xzyw + s1.xzyw*sh.zzww ;

    float3 p0 = (float3)(a0.xy,h.x);
    float3 p1 = (float3)(a0.zw,h.y);
    float3 p2 = (float3)(a1.xy,h.z);
    float3 p3 = (float3)(a1.zw,h.w);

    // Normalise gradients
    float4 norm = taylorInvSqrt((float4)(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    // Mix final noise value
    float4 m = max(0.6f - (float4)(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0f);
    m = m * m;
    return 42.0f * dot( m*m, (float4)( dot(p0,x0), dot(p1,x1),
                dot(p2,x2), dot(p3,x3) ) );
}

inline float4 procedural_wood_sample(float z0, float z1, float4 n, float4 u, float4 v, float4 p, float4 *ko, float4 kd){
  *ko = diffuse_sample_hem(z0, z1, n, u, v);

  float frequency = 2.0f;
  float noiseScale = 6.0f;
  float ringScale = 0.6f;
  float contrast = 4.0f;

  float ptr;

  float noise = snoise(p.xyz);
  float ring = fract( frequency * p.z + noiseScale * noise, &ptr );
  ring *= contrast * ( 1.0f - ring );

  // Adjust ring smoothness and shape, and add some noise
  float lerp = pow( ring, ringScale ) + noise;
  float4 color = mix( kd, kd/3.0f, lerp);

  return color;
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
  float4 bd;
  
  float4 hit_radiance;
  float4 bias = n * BIAS;

  // TODO rempve hardcoded scene properties
  if(hit_id == 6) {
    // big sphere
    hit_radiance = rl_brdf_sample(z0, z1, n, u, v, -rd, &bd, 10, 100000, hit_kd, (float4)(0.2f, 0.2f, 0.2f, 0.f));
  } else if(hit_id == 7) {
    // small sphere
    hit_radiance = glass_bxdf_sample(ray, z0, n, u, v, -rd, &bd, &bias, 1.0f, 1.52f, hit_kd, hit_kd);
  } else if (hit_id == 1) {
    // floor
    hit_radiance = rl_brdf_sample(z0, z1, n, u, v, -rd, &bd, 10, 10000, hit_kd, (float4)(0.8f, 0.8f, 0.8f, 0.f));
  } else if (hit_id == 3){
    // left wall
    hit_radiance = procedural_wood_sample(z0, z1, n, u, v, p, &bd, hit_kd);
  } else {
    hit_radiance = lambertian_brdf_sample(z0, z1, n, u, v, &bd, hit_kd);
  }

  float4 bo = p + bias;

  // accumulate color and radiance
  color[ray] += radiance[ray] * hit_emission;
  radiance[ray] *= hit_radiance;

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