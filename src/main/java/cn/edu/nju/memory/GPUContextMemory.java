package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;


import java.util.*;

public class GPUContextMemory {

    private CUdeviceptr code = new CUdeviceptr();

    private CUdeviceptr type = new CUdeviceptr();

    private static GPUContextMemory gpuContextMemory;

    private GPUContextMemory(List<String> contextStrList) {
        int size = contextStrList.size();

        ContextParser parser = new ContextParser();
        int [] codeRaw = new int[size];
        int [] typeRaw = new int[size];
        for(int i = 0; i < size; i++) {
            Context c = parser.parseContext(i,contextStrList.get(i));
            codeRaw[i] = parseInt(c.getCode());
            typeRaw[i] = c.getType();//Integer.parseInt(c.getPlateNumber());
        }

        cuMemAlloc(this.code, size * Sizeof.INT);
        cuMemcpyHtoD(this.code, Pointer.to(codeRaw), size * Sizeof.INT);

        cuMemAlloc(this.type, size * Sizeof.INT);
        cuMemcpyHtoD(this.type, Pointer.to(typeRaw), size * Sizeof.INT);

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

    private int parseInt(String str) {
        int res = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if(c >= '0' && c <= '9') {
                res = (res + (c - '0')) * 36;
            }
            else {
                res = (res + (c - 'A' + 10)) * 36;
            }
        }
        return res;
    }

    public CUdeviceptr getCode() {
        return code;
    }

    public CUdeviceptr getType() {
        return type;
    }
}
