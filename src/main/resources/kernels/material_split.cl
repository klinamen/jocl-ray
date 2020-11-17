// #define N_MATERIALS 2
// #define LOCAL_QUEUE_SIZE 128
// #define GLOBAL_QUEUE_SIZE 1024

// #define DEBUG_RAY 990679

__kernel void material_split(
        __global const int *hit_map,
        __global const int *id_to_material,
        __global int *ray_queue,
        __global int *queue_index
        ) {

  local int q_local[N_MATERIALS * LOCAL_QUEUE_SIZE];
  local int q_local_index[N_MATERIALS];

  int local_id = get_local_id(0);
  if(local_id < N_MATERIALS){
    q_local_index[local_id] = 0;
  }

  barrier(CLK_LOCAL_MEM_FENCE);

  int ray = get_global_id(0);
  int hit_id = hit_map[ray];
  
  if (hit_id < 0) {
      // no element hit -> terminate
#ifdef DEBUG_RAY
    if(ray == DEBUG_RAY){
      printf("material_split: ray=%d, MISS\n", ray);
    }
#endif

  } else {

    int hit_mat_index = id_to_material[hit_id];
    int old_index = atomic_inc(&q_local_index[hit_mat_index]);
    q_local[hit_mat_index * LOCAL_QUEUE_SIZE + old_index] = ray;

  #ifdef DEBUG_RAY
    if(ray == DEBUG_RAY){
      printf("material_split: ray=%d, hit_mat=%d, q_index_old=%d, abs_queue_index=%d\n", ray, hit_mat_index, old_index, abs_queue_index);
    }
  #endif
  }

  barrier(CLK_LOCAL_MEM_FENCE);

  if(local_id > LOCAL_QUEUE_SIZE - N_MATERIALS - 1){
    int q = local_id - LOCAL_QUEUE_SIZE + N_MATERIALS;

    int q_local_count = q_local_index[q];
    int global_old_count = atomic_add(&queue_index[q], q_local_count);

    for(int i=0; i<q_local_count; i++){
      ray_queue[q * GLOBAL_QUEUE_SIZE + global_old_count + i] = q_local[q * LOCAL_QUEUE_SIZE + i];
    }
  }
}