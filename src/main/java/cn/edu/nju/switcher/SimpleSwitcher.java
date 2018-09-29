package cn.edu.nju.switcher;


import cn.edu.nju.checker.CheckerType;
import cn.edu.nju.util.LogFileHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Properties;

/**
 * Created by njucjc at 2018/7/24
 */
public class SimpleSwitcher implements Switcher {

//    private final static int THRESHOLD = 70;
//
//    private final static int STEP = 5;
    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    private OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

    private long initUpTime = runtimeMXBean.getUptime();

    private long initCPUTime = threadMXBean.getCurrentThreadCpuTime();

    private int nrCPUs = osMxBean.getAvailableProcessors();

    private int maxDelay;

    private int step;

    private float cpuUsageLow;

    //private float cpuUsageHigh;

    private int checkerType;

    private int schedulerType;

    private int count;

    private int totalDelay;

    public SimpleSwitcher(String paramFilePath, int checkerType, int schedulerType) {
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
        this.count = 0;
        this.totalDelay = 0;

        parseParamFile(paramFilePath);
    }

    private void parseParamFile(String paramFilePath) {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(paramFilePath);
            properties.load(fis);
            fis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        this.maxDelay = Integer.parseInt(properties.getProperty("maxDelay"));
        this.step = Integer.parseInt(properties.getProperty("step"));
        this.cpuUsageLow = Float.parseFloat(properties.getProperty("cpuUsageLow"));
       /// this.cpuUsageHigh = Float.parseFloat(properties.getProperty("cpuUsageHigh"));
    }

    @Override
    public synchronized boolean isSwitch(long delay) {
        boolean needSwitch = false;

        if(delay != 0) {
            count = (count + 1) % step;
            totalDelay += delay;
        }

        if(count == 0) {

            switch (checkerType) {
                case CheckerType.ECC_TYPE: {
                    if (totalDelay / step > maxDelay) {
                        needSwitch = true;
                        checkerType = CheckerType.CON_TYPE;
                    }
                    break;
                }

                case CheckerType.CON_TYPE: {
                    if (totalDelay / step > maxDelay) {
                        needSwitch = true;
                        checkerType = CheckerType.PCC_TYPE;
                    }
                    break;
                }

                case CheckerType.PCC_TYPE: {
                    long elapsedCPUTime = threadMXBean.getCurrentThreadCpuTime() - initCPUTime;
                    long elapsedUpTime = runtimeMXBean.getUptime() - initUpTime;

                    float cpuUsage = elapsedCPUTime * 100 / (elapsedUpTime * 1000000F * nrCPUs);

                    if (totalDelay / step > maxDelay) {
                        if(schedulerType == 1) {
                            needSwitch = true;
                            schedulerType = 0;
                        }
                    }
                    else { //Delay normal
                        if (cpuUsage < cpuUsageLow) {
                            needSwitch = true;
                            if (schedulerType == 0) {
                                schedulerType = 1;
                            } else {
                                checkerType = CheckerType.ECC_TYPE;
                            }
                        }
                    }

                    break;
                }

            }
            totalDelay = 0;
        }

        if(needSwitch) {
            //calculate cpu time
            initUpTime = runtimeMXBean.getUptime();
            initCPUTime = threadMXBean.getCurrentThreadCpuTime();
        }

        return needSwitch;
    }

    @Override
    public synchronized int getCheckerType() {
        return checkerType;
    }

    @Override
    public synchronized int getSchedulerType() {
        return schedulerType;
    }
}
