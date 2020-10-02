#define DEG 8
#define DEBUG_RAY 646036

__kernel void
bbTreeSearch(__global const float4 *rayOrigins, __global const float4 *rayDirections,
                  __global int *hitMap,
                  __global const float4 *bbMinVetrices,
                  __global const float4 *bbMaxVertices,
                  int nMeshes, int level) {
  int ray = get_global_id(0);
  // int level = get_global_id(1);

  int bbOffset = 0;
  int nodes = 1;

  if(level > 0){
    int h = hitMap[ray];
    if(h < 0){
      // DEBUG
      return;
    }

    if(level == 1){
      nodes = nMeshes;
      bbOffset = 1;
    } else {
      nodes = DEG;
      bbOffset = DEG * h + 1 - abs(nMeshes - DEG);
    }
  }

  // ray
  float4 ro = rayOrigins[ray];
  float4 rd = rayDirections[ray];

  int bbIndexHit = -1;
  float tHit = 0;

  for(int i=0; i<nodes; i++){
    int bbIndex = bbOffset + i;

    float4 v0 = bbMinVetrices[bbIndex];
    float4 v1 = bbMaxVertices[bbIndex];

    float4 t0 = (v0 - ro) / rd;
    float4 t1 = (v1 - ro) / rd;

    float4 tmin = min(t0, t1);
    float4 tmax = max(t0, t1);

    float tmin_max = max(tmin.x, max(tmin.y, tmin.z));
    float tmax_min = min(tmax.x, min(tmax.y, tmax.z));

    if(tmin_max < tmax_min){
      if(bbIndexHit < 0 || tmin_max < tHit){
        bbIndexHit = bbIndex;
        tHit = tmin_max;
      }
    }

    // if (tmax.x >= tmin.y && tmax.y >= tmin.x) {
    //   float tmin_max = max(tmin.x, max(tmin.y, tmin.z));
    //   float tmax_min = min(tmax.x, min(tmax.y, tmax.z));

    //   if (tmax.z >= tmin_max && tmax_min >= tmin.z) {
    //     float t = tmin_max;
    //     if(t > 0){
    //       // hit!
    //       if(bbIndexHit < 0 || t < tHit){
    //         bbIndexHit = bbIndex;
    //         tHit = t;
    //       }
    //     }
    //   }
    // }

#ifdef DEBUG_RAY
    if (ray == DEBUG_RAY) {
      printf("TreeSearch: ray=%d; level=%d; h=%d; bbOffset=%d; i=%d/%d; bbIndex=%d; bbIndexHit=%d; tHit=%f; bb=(%f, %f, %f)-(%f, %f, %f)\n",
            ray, level, hitMap[ray], bbOffset, i, nodes - 1, bbIndex, bbIndexHit, tHit, v0.x, v0.y, v0.z, v1.x, v1.y, v1.z);
    }
#endif
  }

  hitMap[ray] = bbIndexHit;
}