package cn.edu.nju.memory;

import cn.edu.nju.util.TrafficGraph;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import java.util.List;
import java.util.Map;

import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemcpyHtoD;

/**
 * Created by njucjc at 2020/2/20
 */
public class GPUGraphMemory {

    private CUdeviceptr graph = new CUdeviceptr();

    private CUdeviceptr oppoTable = new CUdeviceptr();

    private static GPUGraphMemory gpuGraphMemory;

    private GPUGraphMemory() {
        initGraph();
        initOppoTable();
    }

    private void initGraph() {
        int [] graphRaw = new int[Config.GRAPH_NODE_NUM * Config.MAX_NEI_NUM];
        for (int i = 0; i < Config.GRAPH_NODE_NUM * Config.MAX_NEI_NUM; i++) {
            graphRaw[i] = -1;
        }
        Map<String, List<String>> trafficGraph = TrafficGraph.getTrafficGraph();
        for (String key : trafficGraph.keySet()) {
            List<String> nei = trafficGraph.get(key);
            int offset = TrafficGraph.codeToInt(key);
            for (int i = 0; i < nei.size(); i++) {
                graphRaw[offset + i] = TrafficGraph.codeToInt(nei.get(i));
            }
        }

        cuMemAlloc(this.graph, Config.GRAPH_NODE_NUM * Config.MAX_NEI_NUM * Sizeof.INT);
        cuMemcpyHtoD(this.graph, Pointer.to(graphRaw), Config.GRAPH_NODE_NUM * Config.MAX_NEI_NUM * Sizeof.INT);

    }

    private void initOppoTable() {
        int [] oppoRaw = new int[Config.GRAPH_NODE_NUM];
        for (int i = 0; i < Config.GRAPH_NODE_NUM ; i++) {
            oppoRaw[i] = -1;
        }
        Map<String, String> opposite = TrafficGraph.getOpposite();
        for (String key : opposite.keySet()) {
            int offset = TrafficGraph.codeToInt(key);
            oppoRaw[offset] = TrafficGraph.codeToInt(opposite.get(key));
        }

        cuMemAlloc(this.oppoTable, Config.GRAPH_NODE_NUM * Sizeof.INT);
        cuMemcpyHtoD(this.oppoTable, Pointer.to(oppoRaw), Config.GRAPH_NODE_NUM * Sizeof.INT);
    }

    public static synchronized GPUGraphMemory getInstance() {
        if(gpuGraphMemory == null) {
            gpuGraphMemory = new GPUGraphMemory();
        }
        return gpuGraphMemory;
    }

    public CUdeviceptr getGraph() {
        return graph;
    }

    public CUdeviceptr getOppoTable() {
        return oppoTable;
    }
}
