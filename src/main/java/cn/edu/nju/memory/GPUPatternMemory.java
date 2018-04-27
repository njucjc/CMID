package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GPUPatternMemory {

    private CUdeviceptr begin = new CUdeviceptr();

    private CUdeviceptr length = new CUdeviceptr();

    private CUdeviceptr contexts = new CUdeviceptr();

    private Map<String, Integer> indexMap = new ConcurrentHashMap<>();

    private static GPUPatternMemory gpuPatternMemory;

    private GPUPatternMemory(Set<String> keySet) {
        int index = 0;
        for(String key : keySet) {
            indexMap.put(key, index);
            index++;
        }

        int num = keySet.size();
        int [] hostData = new int[num];
        for(int i = 0; i < num; i++) {
            hostData[i] = 0;
        }
        cuMemAlloc(this.begin, num * Sizeof.INT);
        cuMemcpyHtoD(this.begin, Pointer.to(hostData), num * Sizeof.INT);

        cuMemAlloc(this.length, num * Sizeof.INT);
        cuMemcpyHtoD(this.length, Pointer.to(hostData), num * Sizeof.INT);

        cuMemAlloc(this.contexts, num * Config.MAX_PATTERN_SIZE * Sizeof.INT);
    }

    public CUdeviceptr getBegin() {
        return this.begin;
    }

    public CUdeviceptr getLength() {
        return this.length;
    }

    public CUdeviceptr getContexts() {
        return this.contexts;
    }

    public Map<String, Integer> getIndexMap() {
        return this.indexMap;
    }

    public synchronized void free() {
//        cuMemFree(this.begin);
//        cuMemFree(this.length);
//        cuMemFree(this.contexts);
    }

    public static synchronized GPUPatternMemory getInstance(Set<String> keySet) {
        if(gpuPatternMemory == null) {
            gpuPatternMemory = new GPUPatternMemory(keySet);
        }
        return gpuPatternMemory;
    }

}
