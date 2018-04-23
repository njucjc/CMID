#include "device_launch_parameters.h"
#include <stdio.h>

enum Type {
	NOT_NODE = 0,
	AND_NODE,
	IMPLIES_NODE,
	UNIVERSAL_NODE,
	EXISTENTIAL_NODE,
	BFUNC_NODE,
	EMPTY_NODE,
	SAME,
	SZ_SPD_CLOSE,
	SZ_LOC_CLOSE,
	SZ_LOC_DIST,
	SZ_LOC_DIST_NEQ ,
	SZ_LOC_RANGE,
	OR_NODE
};

#define MAX_PARAM_NUM 2

__device__ double distance(double latitude1, double latitude2, double longitude1, double longitude2)
{
	return ((latitude1 - latitude2) * (latitude1 - latitude2) + (longitude1 - longitude2) * (longitude1 - longitude2));
}


__device__ bool same(int id1, int id2)
{
	return id1 == id2;
}

__device__ bool sz_spd_close(double speed1, double speed2) 
{
	return ((speed1 - speed2) >= -50.0 && (speed1 - speed2) <= 50.0);
}

__device__ bool sz_loc_close(double latitude1, double latitude2, double longitude1, double longitude2) 
{
	return distance(latitude1, latitude2, longitude1, longitude2) <= 0.000001;
}

__device__ bool sz_loc_dist(double latitude1, double latitude2, double longitude1, double longitude2) 
{
	return  distance(latitude1, latitude2, longitude1, longitude2) <= 0.000625;
}

__device__ bool sz_loc_dist_neq(double latitude1, double latitude2, double longitude1, double longitude2) 
{
	double dist = distance(latitude1, latitude2, longitude1, longitude2);
	bool result = true;
	if (dist > 0.000625 || dist == 0) {
		result = false;
	}
	return result;
	//return (dist <= 0.000625) && (dist != 0);
}

__device__ bool sz_loc_range(double latitude, double longitude)
{
	return longitude >= 112.0 && longitude <= 116.0 && latitude >= 20.0 && latitude <= 24.0;
}

__device__ int calc_offset(	int node, int tid, int *branch_idx,
							int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx,
							int *pattern_length,
							int *branch_size)
{
	int offset = 0;
	int current_node = node;
	int index = 0, tmp = tid;
	while (parent[current_node] != -1) {
		int type = node_type[parent[current_node]];
		if (type == Type::EXISTENTIAL_NODE || type == Type::UNIVERSAL_NODE) {
			int len = pattern_length[pattern_idx[parent[current_node]]];
			branch_idx[index] = tmp % len;
			tmp /= len;
			offset += (branch_idx[index] + 1) * branch_size[current_node];
			index++;
		}
		else if (type == Type::AND_NODE || type == Type::IMPLIES_NODE || type == Type::OR_NODE) {
			if (right_child[parent[current_node]] == current_node) {
				offset += 2 * branch_size[current_node];
			}
			else {
				offset += branch_size[current_node];
			}
		}
		else {
			offset += branch_size[current_node];
		}
		current_node = parent[current_node];
	}
	return offset == 0 ? 0 : offset - 1;
}



extern "C"
__global__ void gen_truth_value(int *parent, int *left_child, int *right_hild, int *node_type, int *pattern_idx, //constraint rule 
								int *branch_size, int cunit_begin, int cunit_end,//cunit_end is the root of cunit
								int pattern_num, int *pattern_begin, int *pattern_length, int *pattern, //patterns
								double *longitude, double *latitude, double *speed, // contexts
								int *truth_values)
{
	int tid = threadIdx.x + blockDim.x * blockIdx.x;
	int branch_idx[MAX_PARAM_NUM];
	int ccopy_root_offset = calc_offset(cunit_end, tid, branch_idx, parent, left_child, right_hild, node_type, pattern_idx, pattern_length, branch_size);

	for (int node = cunit_begin; node <= cunit_end; node++) {
		int offset = ccopy_root_offset - (cunit_end - node);

	}

 }

 extern "C"
 __global__ void gen_links()
 {

  }

extern "C"
__global__ void update_pattern()
{

}