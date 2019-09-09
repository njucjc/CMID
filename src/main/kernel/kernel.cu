#include "device_launch_parameters.h"
#include <stdio.h>

#define NOT_NODE 0
#define AND_NODE 1
#define IMPLIES_NODE 2
#define UNIVERSAL_NODE 3
#define EXISTENTIAL_NODE 4
#define BFUNC_NODE 5
#define EMPTY_NODE 6

#define ELECTRIC_RANGE 13
#define VOLTAGE_RANGE 14
#define ACC_RANG 15
#define ACC_RATE_RANG 16
#define ALL_IN_BRAKE_STATE 17
#define ALL_IN_TRACTION_STATE 18
#define NOT_TRANS_TO_BRAKE 19
#define NOT_TRANS_TO_TRACTION 20
#define IN_BRAKE_STATE 21
#define IN_TRACTION_STATE 22
#define OR_NODE 23

#define MAX_PARAM_NUM 2
#define MAX_CCT_SIZE 3000000
#define MAX_LINK_SIZE 5000
#define DEBUG


#define STOP 0
#define START 1
#define TRACTION 2
#define COAST 3
#define BRAKE 4

struct Context{
	int id;
	double u;
	double i;
	double p;
	double v;
	double a;
	int status;
};

struct Node {
	Node *next;
	Node *tail;
	int params[MAX_PARAM_NUM];
};


__device__ bool truth_values[MAX_CCT_SIZE];
__device__ Node links[MAX_CCT_SIZE];


extern "C"
__device__ double my_abs(double num){
	if (num < 0) {
		return -num;
	}
	else {
		return num;
	}
}

extern "C"
__device__ int now(Context c1, Context c2, int diff){
	if (c1.id - c2.id == diff) {
		return 2;
	}
	else if (c2.id - c1.id == diff) {
		return 1;
	}
	else {
		return 0;
	}
}

extern "C"
__device__ int next(Context c1, Context c2, int diff){
	if (c1.id - c2.id == diff) {
		return 1;
	}
	else if (c2.id - c1.id == diff) {
		return 2;
	}
	else {
		return 0;
	}
}

extern "C"
__device__ bool electric_range(Context c){
	if (c.status != TRACTION && c.status != BRAKE) {
		return true;
	}
	return abs(c.i) <= 740.0;
}

extern "C"
__device__ bool voltage_range(Context c){
	return abs(c.u) >= 1450.0 && abs(c.u) <= 1800.0;
}

extern "C"
__device__ bool acc_range(Context c1, Context c2){
	bool res = true;
	double t = 5.0;
	double v = abs(c1.v - c2.v);

	if (now(c1, c2, 50) != 0) {
		res = (v / t) <= 1.0;
	}
	return res;
}

extern "C"
__device__ bool acc_rate_range(Context c1, Context c2){
	bool res = true;
	double t = 5.0;
	double a = abs(c1.a - c2.a);

	if (now(c1, c2, 50) != 0) {
		res = (a / t) <= 1.5;
	}
	return res;
}

extern "C"
__device__ bool not_trans_to_brake(Context c1, Context c2){
	int no = next(c1, c2, 1);
	if (no == 1) {
		return c1.status != BRAKE;
	}
	else if (no == 2) {
		return c2.status != BRAKE;
	}
	else {
		return false;
	}
}

extern "C"
__device__ bool not_trans_to_traction(Context c1, Context c2){
	int no = next(c1, c2, 1);
	if (no == 1) {
		return c1.status != TRACTION;
	}
	else if (no == 2) {
		return c2.status != TRACTION;
	}
	else {
		return false;
	}
}

extern "C"
__device__ bool in_brake_state(Context c1, Context c2){
	int no = now(c1, c2, 1);
	if (no == 1) {
		return c1.status == BRAKE;
	}
	else if (no == 2) {
		return c2.status == BRAKE;
	}
	else {
		return false;
	}
}

extern "C"
__device__ bool in_traction_state(Context c1, Context c2){
	int no = now(c1, c2, 1);
	if (no == 1) {
		return c1.status == TRACTION;
	}
	else if (no == 2) {
		return c2.status == TRACTION;
	}
	else {
		return false;
	}
}


extern "C"
__device__ bool all_in_brake_state(Context c1, Context c2){
	if (c1.status == BRAKE && c2.status == BRAKE) {
		return now(c1, c2, 50) != 0;
	}
	return false;
}

extern "C"
__device__ bool all_in_traction_state(Context c1, Context c2){
	if (c1.status == TRACTION && c2.status == TRACTION) {
		return now(c1, c2, 50) != 0;
	}
	return false;
}

extern "C"
__device__ void init_node(Node *n){
	n->next = NULL;
	n->tail = n;
	for (int i = 0; i < MAX_PARAM_NUM; i++) {
		n->params[i] = -1;
	}
}

extern "C"
__device__ bool is_null_node(Node *n){
	bool res = true;
	for (int i = 0; i < MAX_PARAM_NUM; i++) {
		res = res && (n->params[i] == -1);
	}
	return res;
}


extern "C"
__device__ void linkHelper(Node *link1, Node *link2) {
	//inital and assumpt that link1 != null, links != null
	if (is_null_node(link1)) {
		for (int i = 0; i < MAX_PARAM_NUM; i++) {
			link1->params[i] = link2->params[i];
		}
		link1->next = NULL;
		link1->tail = link1;

		
		if(link2->next != NULL) {
			link2->next->tail = link2->tail;
		}
		link2 = link2->next;
	}

	if (link2 == NULL) {
		return;
	}

	link1->tail->next = link2;
	link1->tail = link2->tail;
}

extern "C"
__device__ int calc_offset(	int node, int tid, Context *params,
							int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx,
							int *pattern_begin, int *pattern_length, int *pattern,
							double *u_u, double *i_i, double *v_v, double *p_p, double *a_a, int *status, // contexts
							int *branch_size) {

	int offset = branch_size[node];
	int current_node = node;
	int index = 0, tmp = tid;
	while (parent[current_node] != -1) {
		int type = node_type[parent[current_node]];
		if (type == EXISTENTIAL_NODE || type == UNIVERSAL_NODE) {
			int len = pattern_length[pattern_idx[parent[current_node]]];
			int branch_idx = tmp % len;
			tmp /= len;

			params[index].id = pattern[pattern_begin[pattern_idx[parent[current_node]]] + branch_idx];//(pattern + pattern_idx[parent[current_node]] * MAX_PATTERN_SIZE)[(branch_idx + pattern_begin[pattern_idx[parent[current_node]]]) % MAX_PATTERN_SIZE];
			params[index].u = u_u[params[index].id];
			params[index].i = i_i[params[index].id];
			params[index].v = v_v[params[index].id];
			params[index].p = p_p[params[index].id];
			params[index].a = a_a[params[index].id];
			params[index].status = status[params[index].id];

			offset += branch_idx * branch_size[current_node] ;
//			printf("branch_idx = %d, branch_size = %d\n", branch_idx, branch_size[current_node]);
			index++;
		}
		else if (type == AND_NODE || type == IMPLIES_NODE || type == OR_NODE) {
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
                          	 double *u_u, double *i_i, double *v_v, double *p_p, double *a_a, int *status,// contexts
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
											u_u, i_i, v_v, p_p, a_a, status,
											branch_size);

//#ifdef DEBUG
//		printf("root = %d, ccopynum = %d, offset = %d\n",cunit_end, ccopy_num, ccopy_root_offset);
//#endif
		for (int node = cunit_begin; node <= cunit_end; node++) {
			int offset = ccopy_root_offset - (cunit_end - node);
			int type = node_type[node];
			bool value;

			Node* cur_links = &links[offset];
			init_node(cur_links);

			switch(type) {
				case UNIVERSAL_NODE: {
					int step = branch_size[left_child[node]];
					value = true;
					bool first = true;
					for (int i = 0; i < pattern_length[pattern_idx[node]]; i++) {
						value = value && truth_values[offset - (i * step + 1)];
						if(!truth_values[offset - (i * step + 1)]) {
							if(first) {
								init_node(cur_links);
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

				case EXISTENTIAL_NODE: {
					int step = branch_size[left_child[node]];
					value = false;
					bool first = true;
					for (int i = 0; i < pattern_length[pattern_idx[node]]; i++) {
						value = value || truth_values[offset - (i * step + 1)];
						if(truth_values[offset - (i * step + 1)]) {
							if(first) {
								init_node(cur_links);
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

				case AND_NODE: {
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
				case OR_NODE: {
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

				case IMPLIES_NODE: {
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

				case NOT_NODE: {
					value = !truth_values[offset - 1];
					linkHelper(cur_links, &(links[offset - 1]));
					break;
				}

				default : { //BFUNC
					switch(type) {
						case ELECTRIC_RANGE: {
							value = electric_range(params[0]);
							break;
						}

						case VOLTAGE_RANGE: {
							value = voltage_range(params[0]);
							break;
						}

						case ACC_RANG: {
							value = acc_range(params[0], params[1]);
							break;
						}

						case ACC_RATE_RANG: {
							value = acc_rate_range(params[0], params[1]);
							break;
						}

						case ALL_IN_BRAKE_STATE: {
							value = all_in_brake_state(params[0], params[1]);
							break;
						}

						case ALL_IN_TRACTION_STATE: {
							value = all_in_traction_state(params[0], params[1]);
							break;
						}

						case NOT_TRANS_TO_BRAKE: {
							value = not_trans_to_brake(params[0], params[1]);
							break;
						}

						case NOT_TRANS_TO_TRACTION: {
							value = not_trans_to_traction(params[0], params[1]);
							break;
						}

						case IN_BRAKE_STATE: {
							value = in_brake_state(params[0], params[1]);
							break;
						}

						case IN_TRACTION_STATE: {
							value = in_traction_state(params[0], params[1]);
							break;
						}

					}

			
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
	                         link_result[MAX_PARAM_NUM * len + j] = head->params[j];
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
