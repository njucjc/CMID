package cn.edu.nju.memory;

import jcuda.driver.CUdeviceptr;

public class GPUContextMemory implements Config {

    CUdeviceptr longitude = new CUdeviceptr();

    CUdeviceptr latitude = new CUdeviceptr();

    CUdeviceptr speed = new CUdeviceptr();

    public GPUContextMemory() {

    }
}
