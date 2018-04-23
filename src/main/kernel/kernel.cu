#include "device_launch_parameters.h"
#include <stdio.h>


extern "C"
__global__ void gen_truth_value(int *father, int *left_child, int *right_hild, int *node_type, int *pattern_id, //constraint rule 
								int *branch_size, int cunit_root, int cunit_end, //cunit
								int pattern_num, int *pattern_begin, int *pattern_length, int *pattern, //patterns
								int *truth_values)
{
	int tid = threadIdx.x + blockDim.x * blockIdx.x;

 }

 extern "C"
 __global__ void gen_links()
 {

  }

extern "C"
__global__ void update_pattern()
{

}