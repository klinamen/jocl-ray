#define DEBUG_RAY 1291857

__kernel void image_multiply(const float weight, __global float4 *image) {
  int pixel = get_global_id(0);

  float4 old = image[pixel];
  image[pixel] = old * weight;

#ifdef DEBUG_RAY
  if(pixel == DEBUG_RAY){
    printf("image_multiply: px=%d, w=%f, old=(%f, %f, %f), new=(%f, %f, %f)\n",
      pixel, weight, old.x, old.y, old.z, image[pixel].x, image[pixel].y, image[pixel].z);
  }
#endif
}