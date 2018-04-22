package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import java.util.*;

public class GPUPatternMemory {

    Map<String, CUdeviceptr> beginMap = new HashMap<>();

    Map<String, CUdeviceptr> lengthMap = new HashMap<>();

    Map<String, CUdeviceptr> contextsMap = new HashMap<>();

    public GPUPatternMemory(String [] keySet) {
        for(String key : keySet) {
            CUdeviceptr begin = new CUdeviceptr();
            CUdeviceptr length = new CUdeviceptr();
            CUdeviceptr contexts = new CUdeviceptr();

            cuMemAlloc(begin, Sizeof.INT);
            cuMemcpyHtoD(begin, Pointer.to(new int[]{0}), Sizeof.INT);

            cuMemAlloc(length, Sizeof.INT);
            cuMemcpyHtoD(length, Pointer.to(new int[]{0}), Sizeof.INT);

            cuMemAlloc(contexts, Config.MAX_PATTERN_SIZE * Sizeof.INT);

            beginMap.put(key, begin);
            lengthMap.put(key, length);
            contextsMap.put(key, contexts);
        }
    }

    public void free() {
        for(String key : beginMap.keySet()) {
            cuMemFree(beginMap.get(key));
            cuMemFree(lengthMap.get(key));
            cuMemFree(contextsMap.get(key));
        }
    }

}
