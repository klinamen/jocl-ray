__kernel void planeIntersect(const float2 frameSize, const float4 e,
                             const float fov_rad, __global float4 *hitNormals,
                             __global float4 *hitPoints, __global int *hitMap,
                             __global const int *elemIds,
                             __global const float4 *planePos,
                             __global const float4 *planeNormals) {
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

  rd = normalize(rd);

  int hitId = -1;
  float4 n = (float4)(0);
  float4 p = (float4)(0);

  int i = get_global_id(1);

  float4 pPos = planePos[i];
  float4 pNorm = normalize(planeNormals[i]);

  float den = dot(rd, pNorm);

  if (den > 0) {
    float t = dot(pPos - e, pNorm) / den;

    // if (t >= 0 && (hitId < 0 || t < tMin)) {
    if (hitId < 0) {
      p = e + t * rd;
      n = -pNorm; // this should be pNorm!
      hitId = elemIds[i];
    }
  }

  // updates hitmap only if empty or the found intersection is nearer
  if (hitId >= 0 && (hitMap[gid] < 0 || length(p) < length(hitPoints[gid]))) {
    hitMap[gid] = hitId;
    hitNormals[gid] = n;
    hitPoints[gid] = p;
  }
}