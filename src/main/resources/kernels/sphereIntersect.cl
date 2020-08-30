__kernel void sphereIntersect(const float2 frameSize, const float4 e,
                              const float fov_rad, __global const int *sphereIds,
                              __global const float4 *centers,
                              __global const float *radiuses,
                              const uint nSpheres,
                              __global float4 *normals,
                              __global float4 *hitPoints,
                              __global int *hitMap) {
  int gid = get_global_id(0);

  int px = gid % (int)frameSize.x;
  int py = gid / (int)frameSize.x;

  // int nx = frameSize.x;
  // int ny = frameSize.y;

  // // ray = e + t * rd; rd: ray direction
  // float4 rd;
  // rd.x = -frameSize.x / 2 + frameSize.x * (px + 0.5) / nx;
  // rd.y = frameSize.y / 2 - frameSize.y * (py + 0.5) / ny;
  // rd.z = -d;
  // rd.w = 0;

  float aspectRatio = frameSize.x / frameSize.y;
  float4 rd;
  rd.x = (2 * ((px + 0.5) / frameSize.x) - 1) * aspectRatio * tan(fov_rad / 2);
  rd.y = (1 - 2 * ((py + 0.5) / frameSize.y)) * tan(fov_rad / 2);
  rd.z = -1;
  rd.w = 0;

  rd = normalize(rd - e);

  int hitId = -1;
  float4 n = (float4)(0);
  float4 p = (float4)(0);

  float tMin = 0;

  // evaluate intersection for each sphere
  for (int i = 0; i < nSpheres; i++) {
    float4 c = centers[i];
    float r = radiuses[i];

    float discr =
        pow(dot(rd, e - c), 2) - dot(rd, rd) * (dot(e - c, e - c) - pow(r, 2));

    if (discr >= 0) {
      float b = -dot(rd, e - c);
      float t = (b - sqrt(discr)) / dot(rd, rd);
      if (t < 0) {
        t = (b + sqrt(discr)) / dot(rd, rd);
      }

      if (t > 0 && (hitId < 0 || t < tMin)) {
        p = e + t * rd;
        n = (p - c) / r;
        hitId = sphereIds[i];
        tMin = t;
      }
    }
  }

  // updates hitmap only if empty or the found intersection is nearer
  if (hitId >= 0 && (hitMap[gid] < 0 || length(p) < length(hitPoints[gid]))) {
    hitMap[gid] = hitId;
    normals[gid] = n;
    hitPoints[gid] = p;
  }
}



// Matrix44f lookAt(const Vec3f& from, const Vec3f& to, const Vec3f& tmp = Vec3f(0, 1, 0)) 
// { 
//     Vec3f forward = normalize(from - to); 
//     Vec3f right = crossProduct(normalize(tmp), forward); 
//     Vec3f up = crossProduct(forward, right); 
 
//     Matrix44f camToWorld; 
 
//     camToWorld[0][0] = right.x; 
//     camToWorld[0][1] = right.y; 
//     camToWorld[0][2] = right.z; 
//     camToWorld[1][0] = up.x; 
//     camToWorld[1][1] = up.y; 
//     camToWorld[1][2] = up.z; 
//     camToWorld[2][0] = forward.x; 
//     camToWorld[2][1] = forward.y; 
//     camToWorld[2][2] = forward.z; 
 
//     camToWorld[3][0] = from.x; 
//     camToWorld[3][1] = from.y; 
//     camToWorld[3][2] = from.z; 
 
//     return camToWorld; 
// } 