__kernel void viewRay(const float2 frameSize, const float4 e,
                              const float fov_rad,
                              __global float4 *directions) {
  int py = get_global_id(0); // row
  int px = get_global_id(1); // col

  float aspectRatio = frameSize.x / frameSize.y;
  
  float4 rd;
  rd.x = (2 * ((px + 0.5) / frameSize.x) - 1) * aspectRatio * tan(fov_rad / 2);
  rd.y = (1 - 2 * ((py + 0.5) / frameSize.y)) * tan(fov_rad / 2);
  rd.z = -1;
  rd.w = 0;

  rd = normalize(rd - e);

  int i = (int)frameSize.x * py + px;
  direction[i] = rd;
}