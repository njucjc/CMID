package cn.edu.nju.memory;

import static jcuda.driver.JCudaDriver.*;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;
import java.util.*;

public class GPUContextMemory {

    CUdeviceptr longitude = new CUdeviceptr();

    CUdeviceptr latitude = new CUdeviceptr();

    CUdeviceptr speed = new CUdeviceptr();

    public GPUContextMemory(List<String> contextStrList) {
        int size = contextStrList.size();

        cuMemAlloc(this.longitude, size * Sizeof.DOUBLE);
        cuMemAlloc(this.latitude, size * Sizeof.DOUBLE);
        cuMemAlloc(this.speed, size * Sizeof.DOUBLE);

        ContextParser parser = new ContextParser();
        double [] longitudeRaw = new double[size];
        double [] latitudeRaw = new double[size];
        double [] speedRaw = new double[size];

        for(int i = 0; i < size; i++) {
            Context c = parser.parseContext(i,contextStrList.get(i));
            longitudeRaw[i] = c.getLongitude();
            latitudeRaw[i] = c.getLatitude();
            speedRaw[i] = c.getSpeed();
        }

        cuMemcpyHtoD(this.longitude, Pointer.to(longitudeRaw), size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.latitude, Pointer.to(latitudeRaw), size * Sizeof.DOUBLE);
        cuMemcpyHtoD(this.speed, Pointer.to(speedRaw), size * Sizeof.DOUBLE);

    }

    public void free() {
        cuMemFree(this.latitude);
        cuMemFree(this.longitude);
        cuMemFree(this.speed);
    }
}
