#include "device_launch_parameters.h"
#include <stdio.h>
/*
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
};*/

#define NOT_NODE 0
#define AND_NODE 1
#define IMPLIES_NODE 2
#define UNIVERSAL_NODE 3
#define EXISTENTIAL_NODE 4
#define BFUNC_NODE 5
#define EMPTY_NODE 6
#define OR_NODE 7

#define BEFORE 8
#define GATE 9
#define EQUAL 10
#define CONN 11
#define OPPO 12
#define NEXT 13

#define MAX_PARAM_NUM 4
#define MAX_CCT_SIZE 3000000
#define MAX_LINK_SIZE 5000

#define GRAPH_NODE_NUM 2000
#define MAX_NEI_NUM 5

#define DEBUG

struct Context{
	int id;
	int code;
	int type;
};

struct Node {
	Node *next;
	Node *tail;
	int params[MAX_PARAM_NUM];
};


__device__ bool truth_values[MAX_CCT_SIZE];
__device__ Node links[MAX_CCT_SIZE];

extern "C"
__device__ bool before(Context c[], int len){
	int tmp = -1;
	for (int i = 0; i < len; i++) {
		if (tmp >= c[i]) return false;
		tmp = c[i];
	}
}

extern "C"
__device__ bool gate(Context c1, Context c2){
	return c1.type == 3 || c2.type == 3;
}

extern "C"
__device__ bool equal(Context c1, Context c2){
	return c1.code == c2.code;
}

extern "C"
__device__ bool conn(int *graph, Context c1, Context c2, int k){
	return has_path(graph, c1.code, c2.code, k);
}

extern "C"
__device__ bool oppo(int *oppo_table, Context c1, Context c2){
	return oppo_table[c1.code] == c2.code;
}

extern "C"
__device__ bool oppo(Context c1, Context c2){
	return c2.id - c1.id == 1;
}

extern "C"
__device__ bool has_path(int *graph, int v, int w, int k){
	bool visited[GRAPH_NODE_NUM];
	for (int i = 0; i < GRAPH_NODE_NUM; i++) {
		visited[i] = false;
	}

	return has_path_k(graph, visited, v, w, k);
}

extern "C"
__device__ bool has_path_k(int *graph, int visited[], int v, int w, int k){
	visited[v] = true;
	if (v == w && k == 0) {
		return true;
	}
	else if (k > 0) {
		int offset = v * MAX_NEI_NUM;
		for (int i = 0; i < MAX_NEI_NUM; i++) {
			if (graph[offset + i] != -1) {
				if (!visited[graph[offset + i]] && has_path_k(graph, visited, graph[offset + i], w, k -1)) return true;
				visited[graph[offset + i]] = false;
			}
			else {
				break;
			}
		}
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
							int *codes, int *types, // contexts
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
			params[index].code = codes[params[index].id];
			params[index].type = types[params[index].id];

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
__device__ void reorder_params(int *oppo_table, int *params_order, Context params[], Context ordered_params[]) {
	int len = params_order[0];
	for (int i = 1; i <= len; i++) {
		if (params_order[i] == 0) continue;
		else if (params_order[i] < 0){
			ordered_params[-params_order[i]] = params[i-1]
			ordered_params[-params_order[i]].code = oppo_table[ordered_params[-params_order[i]].code];
		}
		else {
			ordered_params[params_order[i]] = params[i-1];
		}

	}
}

extern "C"
__global__ void evaluation(int *parent, int *left_child, int *right_child, int *node_type, int *pattern_idx, //constraint rule
                          	 int *branch_size, int cunit_begin, int cunit_end,//cunit_end is the root of cunit
                          	 int *pattern_begin, int *pattern_length, int *pattern, //patterns
                          	 int *codes, int *types,// contexts
                          	 int *graph, int *oppo_table,
                          	 int *params_order,
                          	 short *truth_value_result,
                          	 int *link_result, int *link_num, int *cur_link_size,
                          	 int last_cunit_root,
                          	 int ccopy_num) {
	
	
	int tid = threadIdx.x + blockDim.x * blockIdx.x;
	if(tid < ccopy_num) {

		Context params[MAX_PARAM_NUM];
		Context ordered_params[MAX_PARAM_NUM];

		for (int i = 0; i < MAX_PARAM_NUM; i++) {
            params[i].id = -1;
            ordered_params[i].id = -1;
         }

		int ccopy_root_offset = calc_offset(cunit_end, tid, params,
											parent, left_child, right_child, node_type, pattern_idx,
											pattern_begin, pattern_length, pattern,
											codes, types,
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
						case BEFORE: {
							reorder_params(oppo_table, params_order, params, ordered_params);
							value = BEFORE(ordered_params, params_order[0]);
							break;
						}

						case GATE: {
							int *gate_params_order = params_order + (GATE - BEFORE) * (MAX_PATTERN_SIZE + 2);
							reorder_params(oppo_table, gate_params_order, params, ordered_params);
							value = gate(ordered_params[0], ordered_params[1]);
							break;
						}

						case EQUAL: {
							int *equal_params_order = params_order + (EQUAL - BEFORE) * (MAX_PATTERN_SIZE + 2);
							reorder_params(oppo_table, equal_params_order, params, ordered_params);
							value = equal(ordered_params[0], ordered_params[1]);
							break;
						}

						case CONN: {
							int *conn_params_order = params_order + (CONN - BEFORE) * (MAX_PATTERN_SIZE + 2);
							reorder_params(oppo_table, conn_params_order, params, ordered_params);
							value = conn(graph, params[0], params[1], conn_params_order[conn_params_order[0]]);
							break;
						}

						case OPPO: {
							int *oppo_params_order = params_order + (OPPO - BEFORE) * (MAX_PATTERN_SIZE + 2);
							reorder_params(oppo_table, oppo_params_order, params, ordered_params);
							value = oppo(oppo_table, ordered_params[0], ordered_params[1]);
							break;
						}

						case NEXT: {
						    int *next_params_order = params_order + (NEXT - BEFORE) * (MAX_PATTERN_SIZE + 2);
						    reorder_params(oppo_table, next_params_order, params, ordered_params);
						    value = next(ordered_params[0], ordered_params[1]);
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
