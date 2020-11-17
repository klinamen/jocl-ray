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

__kernel void scatter_proc(
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

  float4 hit_throughput = procedural_wood_sample(z0, z1, n, u, v, p, &bd, hit_kd);
  
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