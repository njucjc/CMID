package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.util.TrafficGraph;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;


import java.util.*;

public class GPUContextMemory {

    private CUdeviceptr code = new CUdeviceptr();

    private CUdeviceptr type = new CUdeviceptr();

    private static GPUContextMemory gpuContextMemory;

    private GPUContextMemory(List<String> contextStrList) {

    }

    public static synchronized GPUContextMemory getInstance(List<String> contextStrList) {
        if(gpuContextMemory == null) {
            gpuContextMemory = new GPUContextMemory(contextStrList);
        }
        return gpuContextMemory;
    }

    public synchronized void free() {
//        cuMemFree(this.latitude);
//        cuMemFree(this.longitude);
//        cuMemFree(this.speed);
//        cuMemFree(this.plateNumber);
    }

    public CUdeviceptr getCode() {
        return code;
    }

    public CUdeviceptr getType() {
        return type;
    }
}
