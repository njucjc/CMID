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
#define MAX_PATTERN_SIZE 500
#define MAX_LINK_SIZE 40
#define DEBUG

struct Context{
	int id;
	double latitude;
	double longitude;
	double speed;
};

struct Links {
	int length;
	int link_pool[MAX_LINK_SIZE][MAX_PARAM_NUM];
};

extern "C"
__device__ bool same(Context c1, Context c2){
	return (c1.id == c2.id);
}

extern "C"
__device__ bool sz_spd_close(Context c1, Context c2){
	return ((c1.speed - c2.speed) >= -50.0 && (c1.speed - c2.speed) <= 50.0);
}

extern "C"
__device__ bool sz_loc_close(Context c1, Context c2){
	return ((c1.latitude - c2.latitude) * (c1.latitude - c2.latitude) + (c1.longitude - c2.longitude) * (c1.longitude - c2.longitude)) <= 0.000001;
}

extern "C"
__device__ bool sz_loc_dist(Context c1, Context c2){
	return ((c1.latitude - c2.latitude) * (c1.latitude - c2.latitude) + (c1.longitude - c2.longitude) * (c1.longitude - c2.longitude)) <= 0.000625;
}

extern "C"
__device__ bool sz_loc_dist_neq(Context c1, Context c2){
	double dist = ((c1.latitude - c2.latitude) * (c1.latitude - c2.latitude) + (c1.longitude - c2.longitude) * (c1.longitude - c2.longitude));
	bool result = true;
    if (dist > 0.000625 || dist == 0) {
    	result = false;
    }
    return result;
	//return (dist <= 0.000625) && (dist != 0);
}

extern "C"
__device__ bool sz_loc_range(Context c){
	return c.longitude >= 112.0 && c.longitude <= 116.0 && c.latitude >=20.0 && c.latitude <= 24.0;
}

extern "C"
__device__ void linkHelper(Links *left, Links *right) {
	int left_len = left->length;
	int right_len = right->length;


	for (int i = 0; i < right_len; i++) {
		int j;
		for (j = 0; j < left_len; j++) {
			if (right->link_pool[i][0] == left->link_pool[j][0] && right->link_pool[i][1] == left->link_pool[j][1]) {
				break;
			}
		}
		if (j == left_len) {
			left->link_pool[left->length][0] = right->link_pool[i][0];
			left->link_pool[left->length][1] = right->link_pool[i][1];
			left->length = (left->length + 1) % MAX_LINK_SIZE;
		}

	}
}

extern "C"
__device__ int calc_offset(	int node, int tid, Context *params,
							int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx,
							int *pattern_begin, int *pattern_length, int *pattern,
							double *longitude, double *latitude, double *speed, // contexts
							int *branch_size) {

	int offset = branch_size[node];
	int current_node = node;
	int index = 0, tmp = tid;
	while (parent[current_node] != -1) {
		int type = node_type[parent[current_node]];
		if (type == Type::EXISTENTIAL_NODE || type == Type::UNIVERSAL_NODE) {
			int len = pattern_length[pattern_idx[parent[current_node]]];
			int branch_idx = tmp % len;
			tmp /= len;

			params[index].id = (pattern + pattern_idx[parent[current_node]] * MAX_PATTERN_SIZE)[(branch_idx + pattern_begin[pattern_idx[parent[current_node]]]) % MAX_PATTERN_SIZE];
			params[index].latitude = latitude[params[index].id];
			params[index].longitude = longitude[params[index].id];
			params[index].speed = speed[params[index].id];

			offset += branch_idx * branch_size[current_node] ;
//			printf("branch_idx = %d, branch_size = %d\n", branch_idx, branch_size[current_node]);
			index++;
		}
		else if (type == Type::AND_NODE || type == Type::IMPLIES_NODE || type == Type::OR_NODE) {
			if (right_child[parent[current_node]] == current_node) {
				offset += branch_size[left_child[parent[current_node]]];
			}
		}
		else {
		    offset += 0;
		}
		current_node = parent[current_node];
	}
	return offset - 1;
}



extern "C"
__global__ void gen_truth_value(int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx, //constraint rule
								int *branch_size, int cunit_begin, int cunit_end,//cunit_end is the root of cunit
								int *pattern_begin, int *pattern_length, int *pattern, //patterns
								double *longitude, double *latitude, double *speed, // contexts
								short *truth_values, int ccopy_num) {
	
	
	int tid = threadIdx.x + blockDim.x * blockIdx.x;
	if(tid < ccopy_num) {

		Context params[MAX_PARAM_NUM];
		int ccopy_root_offset = calc_offset(cunit_end, tid, params,
											parent, left_child, right_child, node_type, pattern_idx,
											pattern_begin, pattern_length, pattern,
											longitude, latitude, speed,
											branch_size);
//#ifdef DEBUG
//		printf("root = %d, ccopynum = %d, offset = %d\n",cunit_end, ccopy_num, ccopy_root_offset);
//#endif
		for (int node = cunit_begin; node <= cunit_end; node++) {
			int offset = ccopy_root_offset - (cunit_end - node);
			int type = node_type[node];
			bool value;
			if(type == Type::UNIVERSAL_NODE) {
				int step = branch_size[left_child[node]];
				value = true;
				for (int i = 0; i < pattern_length[pattern_idx[node]]; i++) {
					value = value && truth_values[offset - (i * step + 1)];
				}
			}
			else if (type == Type::EXISTENTIAL_NODE) {
				int step = branch_size[left_child[node]];
				value = false;
				for (int i = 0; i < pattern_length[pattern_idx[node]]; i++) {
					value = value || truth_values[offset - (i * step + 1)];
				}
			}
			else if (type == Type::AND_NODE) {
				//right && left
				value = truth_values[offset - 1] && truth_values[offset - (branch_size[right_child[node]] + 1)];
			}
			else if (type == Type::OR_NODE) {
				//right || left
				value = truth_values[offset - 1] || truth_values[offset - (branch_size[right_child[node]] + 1)];
			}
			else if (type == Type::IMPLIES_NODE) {
				//!left || right
				value = !truth_values[offset - (branch_size[right_child[node]] + 1)] || truth_values[offset - 1];
			}
			else if (type == Type::NOT_NODE) {
				value = !truth_values[offset - 1];
			}
			else if (type == Type::SAME) {
				value = same(params[0], params[1]);
			}
			else if (type == Type::SZ_SPD_CLOSE) {
				value = sz_spd_close(params[0], params[1]);
			}
			else if (type == Type::SZ_LOC_CLOSE) {
				value = sz_loc_close(params[0], params[1]);
			}
			else if (type == Type::SZ_LOC_DIST) {
				value = sz_loc_dist(params[0], params[1]);
			}
			else if (type == Type::SZ_LOC_DIST_NEQ) {
				value = sz_loc_dist_neq(params[0], params[1]);
			}
			else if (type == Type::SZ_LOC_RANGE) {
				value = sz_loc_range(params[0]);
			}
			truth_values[offset] = value;
		}
	}

 }

 extern "C"
 __global__ void gen_links(int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx, //constraint rule
	 int *branch_size, int cunit_begin, int cunit_end,//cunit_end is the root of cunit
	 int *pattern_begin, int *pattern_length, int *pattern, //patterns
	 double *longitude, double *latitude, double *speed, // contexts
	 short *truth_values,
	 Links *links, int *link_result, int *link_num,
	 int last_cunit_root,
	 int ccopy_num) {

	 int tid = threadIdx.x + blockDim.x * blockIdx.x;
	 if(tid < ccopy_num) {
		 Context params[MAX_PARAM_NUM];

		 for (int i = 0; i < MAX_PARAM_NUM; i++) {
			 params[i].id = -1;
		 }

		 int ccopy_root_offset = calc_offset(cunit_end, tid, params,
			 parent, left_child, right_child, node_type, pattern_idx,
			 pattern_begin, pattern_length, pattern,
			 longitude, latitude, speed,
			 branch_size);

		 for (int node = cunit_begin; node <= cunit_end; node++) {
			 int offset = ccopy_root_offset - (cunit_end - node);
			 int type = node_type[node];
			 bool value = truth_values[offset];
			 Links* cur_links = &links[offset];
			 cur_links->length = 0;
			 if (type == Type::UNIVERSAL_NODE || type == Type::EXISTENTIAL_NODE) {
				 int step = branch_size[left_child[node]];
				 for (int i = 0; i < pattern_length[pattern_idx[node]]; i++) {
					 if (truth_values[offset - (i * step + 1)] == value) {
						 linkHelper(cur_links, &links[offset - (i * step + 1)]);
					 }
				 }
//				 printf("length = %d\n", cur_links->length);
//				 for(int i = 0; i < cur_links->length; i++) {
//				    printf("%d %d\n", cur_links->link_pool[i][0], cur_links->link_pool[i][1]);
//				 }
			 }
			 else if (type == Type::AND_NODE || type == Type::OR_NODE) {
				 if (truth_values[offset - 1] == value) {
					 linkHelper(cur_links, &links[offset - 1]);
				 }

				 if (truth_values[offset - (branch_size[right_child[node]] + 1)] == value) {
					 linkHelper(cur_links, &links[offset - (branch_size[right_child[node]] + 1)]);
				 }
			 }
			 else if (type == Type::IMPLIES_NODE) {
				 //!left || right
				 bool left = truth_values[offset - (branch_size[right_child[node]] + 1)];
				 bool right = truth_values[offset - 1];

				 if ((!left && right) || left && !right) {
					 linkHelper(cur_links, &links[offset - 1]);
					 linkHelper(cur_links, &links[offset - (branch_size[right_child[node]] + 1)]);
				 }
				 else if(left && right){
					 linkHelper(cur_links, &links[offset - 1]);
				 }
				 else if (left && right) {
					 linkHelper(cur_links, &links[offset - (branch_size[right_child[node]] + 1)]);
				 }
			 }
			 else if (type == Type::NOT_NODE) {
				 linkHelper(cur_links, &links[offset - 1]);
			 }
			 else if (type == Type::SAME
				 || type == Type::SZ_SPD_CLOSE
				 || type == Type::SZ_LOC_CLOSE
				 || type == Type::SZ_LOC_DIST
				 || type == Type::SZ_LOC_DIST_NEQ
				 || type == Type::SZ_LOC_RANGE) {
				 cur_links->length = 1;
				 for (int i = 0; i < MAX_PARAM_NUM; i++) {
					 cur_links->link_pool[0][i] = params[i].id;
				 }
			 }

		 }

		 if (last_cunit_root == cunit_end) {
         	*link_num = links[ccopy_root_offset].length;
         		for (int i = 0; i < *link_num; i++) {
         			link_result[i] = links[ccopy_root_offset].link_pool[i][0];
         			link_result[i + 1] = links[ccopy_root_offset].link_pool[i][1];
         		}
          }
	 }

  }

extern "C"
__global__ void update_pattern(int op, int pattern_idx,
							   int *pattern_begin, int *pattern_length, int *pattern,
							   int id) {
	if (op == 0) { //-
		pattern_begin[pattern_idx] = (pattern_begin[pattern_idx] + 1) % MAX_PATTERN_SIZE;
		pattern_length[pattern_idx]--;
	}
	else if (op == 1) {//+
		(pattern + pattern_idx * MAX_PATTERN_SIZE)[(pattern_begin[pattern_idx] + pattern_length[pattern_idx]) % MAX_PATTERN_SIZE] = id;
		pattern_length[pattern_idx]++;

	}

}