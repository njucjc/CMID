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
#define MAX_CCT_SIZE 3000000
#define MAX_LINK_SIZE 500
#define DEBUG

struct Context{
	int id;
	double latitude;
	double longitude;
	double speed;
	int plateNumber;
};

struct Node {
	Node *next;
	int params[MAX_PARAM_NUM];
};


__device__ bool truth_values[MAX_CCT_SIZE];
__device__ Node links[MAX_CCT_SIZE];

extern "C"
__device__ bool same(Context c1, Context c2){
	return (c1.plateNumber == c2.plateNumber);
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
__device__ void linkHelper(Node *link1, Node *link2) {
	//inital and assumpt that link1 != null;

	if (link2 == NULL) {
		return;
	}

	Node *tail = link1;
	int len = 1;
	while(tail->next != NULL){
		tail = tail->next;
		len++;
	}

	for(Node *cur = link2; cur != NULL; ) {
		Node *p = link1;
		int i;
		for(i = 0; i < len; i++) {
			if(p->params[0] == cur->params[0] && p->params[1] == cur->params[1]) {
				break;
			}
			else {
				p = p->next;
			}
		}

		if(i == len) {
			q = cur;
			cur = cur->next;

			tail->next = q;
			q->next = NULL;
			tail = q;
		}
		else {
			cur = cur->next;
		}

	}
}

extern "C"
__device__ int calc_offset(	int node, int tid, Context *params,
							int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx,
							int *pattern_begin, int *pattern_length, int *pattern,
							double *longitude, double *latitude, double *speed, int *plateNumber, // contexts
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

			params[index].id = pattern[pattern_begin[pattern_idx[parent[current_node]]] + branch_idx];//(pattern + pattern_idx[parent[current_node]] * MAX_PATTERN_SIZE)[(branch_idx + pattern_begin[pattern_idx[parent[current_node]]]) % MAX_PATTERN_SIZE];
			params[index].latitude = latitude[params[index].id];
			params[index].longitude = longitude[params[index].id];
			params[index].speed = speed[params[index].id];
			params[index].plateNumber = plateNumber[params[index].id];

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
__global__ void evaluation(int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx, //constraint rule
                          	 int *branch_size, int cunit_begin, int cunit_end,//cunit_end is the root of cunit
                          	 int *pattern_begin, int *pattern_length, int *pattern, //patterns
                          	 double *longitude, double *latitude, double *speed,int *plateNumber,// contexts
                          	 short *truth_value_result,
                          	 int *link_result, int *link_num, int *cur_link_size,
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
											longitude, latitude, speed, plateNumber,
											branch_size);

//#ifdef DEBUG
//		printf("root = %d, ccopynum = %d, offset = %d\n",cunit_end, ccopy_num, ccopy_root_offset);
//#endif
		for (int node = cunit_begin; node <= cunit_end; node++) {
			int offset = ccopy_root_offset - (cunit_end - node);
			int type = node_type[node];
			bool value;

			Node* cur_links = &links[offset];
			cur_links->next = NULL;

			switch(type) {
				case Type::UNIVERSAL_NODE: {
					int step = branch_size[left_child[node]];
					value = true;
					bool first = true;
					for (int i = 0; i < pattern_length[pattern_idx[node]]; i++) {
						value = value && truth_values[offset - (i * step + 1)];
						if(!truth_values[offset - (i * step + 1)]) {
							if(first) {
								cur_links->next = NULL;
								first = false;
							}
							linkHelper(cur_links, &(links[offset - (i * step + 1)]));
						}
						else if(value) {
							linkHelper(cur_links, &(links[offset - (i * step + 1)]));
						}
					}

					break;
				}

				case Type::EXISTENTIAL_NODE: {
					int step = branch_size[left_child[node]];
					value = false;
					bool first = true;
					for (int i = 0; i < pattern_length[pattern_idx[node]]; i++) {
						value = value || truth_values[offset - (i * step + 1)];
						if(truth_values[offset - (i * step + 1)]) {
							if(first) {
								cur_links->next= NULL;
								first = false;
							}
							linkHelper(cur_links, &(links[offset - (i * step + 1)]));
						}
						else if(!value) {
							linkHelper(cur_links, &(links[offset - (i * step + 1)]));
						}
					}
					break;
				}

				case Type::AND_NODE: {
					//right && left
					value = truth_values[offset - 1] && truth_values[offset - (branch_size[right_child[node]] + 1)];

					if (truth_values[offset - 1] == value) {
						linkHelper(cur_links, &(links[offset - 1]));
					}

					if (truth_values[offset - (branch_size[right_child[node]] + 1)] == value) {
						linkHelper(cur_links, &(links[offset - (branch_size[right_child[node]] + 1)]));
					}

					break;
				}
				case Type::OR_NODE: {
					//right || left
					value = truth_values[offset - 1] || truth_values[offset - (branch_size[right_child[node]] + 1)];

					if (truth_values[offset - 1] == value) {
						linkHelper(cur_links, &(links[offset - 1]));
					}

					if (truth_values[offset - (branch_size[right_child[node]] + 1)] == value) {
						linkHelper(cur_links, &(links[offset - (branch_size[right_child[node]] + 1)]));
					}

					break;
				}

				case Type::IMPLIES_NODE: {
					//!left || right
					value = !truth_values[offset - (branch_size[right_child[node]] + 1)] || truth_values[offset - 1];

					if(value) {
	                   linkHelper(cur_links, &(links[offset - 1]));
	                   linkHelper(cur_links, &(links[offset - (branch_size[right_child[node]] + 1)]));
					}
					else {
					   linkHelper(cur_links, &(links[offset - 1]));
					}

					break;
				}

				case Type::NOT_NODE: {
					value = !truth_values[offset - 1];
					linkHelper(cur_links, &(links[offset - 1]));
					break;
				}

				default : { //BFUNC
					switch(type) {
						case Type::SAME: {
							value = same(params[0], params[1]);
							break;
						}

						case Type::SZ_SPD_CLOSE: {
							value = sz_spd_close(params[0], params[1]);
							break;
						}

						case Type::SZ_LOC_CLOSE: {
							value = sz_loc_close(params[0], params[1]);
							break;
						}

						case Type::SZ_LOC_DIST: {
							value = sz_loc_dist(params[0], params[1]);
							break;
						}

						case Type::SZ_LOC_DIST_NEQ: {
							value = sz_loc_dist_neq(params[0], params[1]);
							break;
						}

						case Type::SZ_LOC_RANGE: {
							value = sz_loc_range(params[0]);
							break;
						}
					}

					cur_links->next = NULL;
					for (int i = 0; i < MAX_PARAM_NUM; i++) {
						cur_links->params[i] = params[i].id;
					}
					break;
				}

				
			}

			truth_values[offset] = value;
		}

		if (last_cunit_root == cunit_end ) {
		    *truth_value_result = truth_values[ccopy_root_offset];
		    if(!truth_values[ccopy_root_offset]) {
            
         		int len = 0;
                for(Node *head = &links[ccopy_root_offset]; head != NULL; head = head ->next) {
                
                	if(len < MAX_LINK_SIZE) {
	                	for(int j = 0; j < MAX_PARAM_NUM; j++) {
	                         link_result[MAX_PARAM_NUM * len + j] = links[ccopy_root_offset].params[j];
	                    }
                	}

                    len++;
                }
                
                *cur_link_size = len;
                *link_num = len > MAX_LINK_SIZE ? MAX_LINK_SIZE : len;
         	}
        }
	}

 }