__kernel void image_multiply(const float weight, __global float *image) {
  int pixel = get_global_id(0);
  image[pixel] = image[pixel] * weight;
}