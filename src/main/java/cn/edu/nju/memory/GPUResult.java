package cn.edu.nju.memory;

import jcuda.Sizeof;
import jcuda.driver.CUdeviceptr;

import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemFree;

public class GPUResult {
    private CUdeviceptr deviceTruthValue = new CUdeviceptr();

    private CUdeviceptr deviceTruthValueResult = new CUdeviceptr();

    private CUdeviceptr deviceLinks = new CUdeviceptr();

    private CUdeviceptr deviceLinkResult = new CUdeviceptr();

    private CUdeviceptr deviceLinkNum = new CUdeviceptr();


    public GPUResult() {
        cuMemAlloc(this.deviceTruthValue, Config.MAX_CCT_SIZE * Sizeof.SHORT);
        cuMemAlloc(this.deviceTruthValueResult, Sizeof.SHORT);
        cuMemAlloc(this.deviceLinks, (1 + Config.MAX_PARAN_NUM * Config.MAX_LINK_SIZE) * Sizeof.INT * Config.MAX_CCT_SIZE);
        cuMemAlloc(this.deviceLinkResult, (Config.MAX_PARAN_NUM * Config.MAX_LINK_SIZE) * Sizeof.INT);
        cuMemAlloc(this.deviceLinkNum, Sizeof.INT);
    }

    public CUdeviceptr getDeviceTruthValue() {
        return deviceTruthValue;
    }

    public CUdeviceptr getDeviceTruthValueResult() {
        return deviceTruthValueResult;
    }

    public CUdeviceptr getDeviceLinks() {
        return deviceLinks;
    }

    public CUdeviceptr getDeviceLinkResult() {
        return deviceLinkResult;
    }

    public CUdeviceptr getDeviceLinkNum() {
        return deviceLinkNum;
    }

    public synchronized void free() {
//        cuMemFree(this.deviceTruthValue);
//        cuMemFree(this.deviceTruthValueResult);
//        cuMemFree(this.deviceLinks);
//        cuMemFree(this.deviceLinkResult);
//        cuMemFree(this.deviceLinkNum);
    }
}
