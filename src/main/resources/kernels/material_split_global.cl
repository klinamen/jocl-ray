// #define DEBUG_RAY 990679

__kernel void material_split_global(
        __global const int *hit_map,
        __global const int *id_to_material,
        const int queue_size,
        __global int *ray_queue,
        __global int *queue_index
        ) {
  int ray = get_global_id(0);

  // no element hit -> terminate
  int hit_id = hit_map[ray];
  if (hit_id < 0) {
#ifdef DEBUG_RAY
    if(ray == DEBUG_RAY){
      printf("material_split: ray=%d, MISS\n", ray);
    }
#endif

    return;
  }

  int hit_mat_index = id_to_material[hit_id];

  volatile __global int* counterPtr = queue_index + hit_mat_index; 
  int old_index = atomic_inc(counterPtr);

  int abs_queue_index = hit_mat_index * queue_size + old_index;
  ray_queue[abs_queue_index] = ray;

#ifdef DEBUG_RAY
  if(ray == DEBUG_RAY){
    printf("material_split: ray=%d, hit_mat=%d, q_index_old=%d, abs_queue_index=%d\n", ray, hit_mat_index, old_index, abs_queue_index);
  }
#endif
}