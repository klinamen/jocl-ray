__kernel void vectorAdd(__global const float4 *v, __global const float4 *w,
                        __global float4 *out) {
  int gid = get_global_id(0);
  out[gid] = v[gid] + w[gid];
}