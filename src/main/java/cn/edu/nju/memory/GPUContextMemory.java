package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;


import java.util.*;

public class GPUContextMemory {

    private CUdeviceptr u = new CUdeviceptr();

    private CUdeviceptr i = new CUdeviceptr();

    private CUdeviceptr p = new CUdeviceptr();

    private CUdeviceptr v = new CUdeviceptr();

    private CUdeviceptr a = new CUdeviceptr();

    private CUdeviceptr status = new CUdeviceptr();


    private static GPUContextMemory gpuContextMemory;

    private GPUContextMemory(List<String> contextStrList) {
        int size = contextStrList.size();

        ContextParser parser = new ContextParser();
        double [] uRaw = new double[size];
        double [] iRaw = new double[size];
        double [] powerRaw = new double[size];
        double [] speedRaw = new double[size];
        double [] aRaw = new double[size];
        int [] statusRaw = new int[size];

        for(int i = 0; i < size; i++) {
            Context c = parser.parseContext(i,contextStrList.get(i));
            uRaw[i] = c.getU();
            iRaw[i] = c.getI();
            powerRaw[i] = c.getP();//Integer.parseInt(c.getPlateNumber());
            speedRaw[i] = c.getV();
            aRaw[i] = c.getA();
            statusRaw[i] = c.getStatus();
        }

        cuMemAlloc(this.u, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.u, Pointer.to(uRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.i, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.i, Pointer.to(iRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.p, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.p, Pointer.to(powerRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.v, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.v, Pointer.to(speedRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.a, size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.a, Pointer.to(aRaw), size * Sizeof.DOUBLE);

        cuMemAlloc(this.status, size * Sizeof.INT);
        cuMemcpyHtoD(this.status, Pointer.to(statusRaw), size * Sizeof.INT);

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

    public CUdeviceptr getU() {
        return u;
    }

    public CUdeviceptr getI() {
        return i;
    }

    public CUdeviceptr getP() {
        return p;
    }

    public CUdeviceptr getV() {
        return v;
    }

    public CUdeviceptr getA() {
        return a;
    }

    public CUdeviceptr getStatus() {
        return status;
    }
}
