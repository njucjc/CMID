package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;


import java.util.*;

public class GPUContextMemory {

    private CUdeviceptr v = new CUdeviceptr();

    private CUdeviceptr i = new CUdeviceptr();

    private CUdeviceptr speed = new CUdeviceptr();

    private CUdeviceptr power = new CUdeviceptr();


    private static GPUContextMemory gpuContextMemory;

    private GPUContextMemory(List<String> contextStrList) {
        int size = contextStrList.size();

        ContextParser parser = new ContextParser();
        double [] vRaw = new double[size];
        double [] iRaw = new double[size];
        double [] speedRaw = new double[size];

        double [] powerRaw = new double[size];

        for(int i = 0; i < size; i++) {
            Context c = parser.parseContext(i,contextStrList.get(i));
            vRaw[i] = c.getV();
            iRaw[i] = c.getI();
            speedRaw[i] = c.getSpeed();
            powerRaw[i] = c.getPower();//Integer.parseInt(c.getPlateNumber());
        }

        cuMemAlloc(this.v, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.v, Pointer.to(vRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.i, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.i, Pointer.to(iRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.speed, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.speed, Pointer.to(speedRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.power, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.power, Pointer.to(powerRaw), size * Sizeof.DOUBLE);

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

    public CUdeviceptr getV() {
        return v;
    }

    public CUdeviceptr getI() {
        return i;
    }

    public CUdeviceptr getSpeed() {
        return speed;
    }

    public CUdeviceptr getPower() {
        return power;
    }
}
