package cn.edu.nju.switcher;


import cn.edu.nju.checker.CheckerType;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Properties;

/**
 * Created by njucjc at 2018/7/24
 */
public class SimpleSwitcher implements Switcher {

    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    private long initUpTime;

    private long initCPUTime;

    private int checkerType;

    private int schedulerType;

    private int count;

    private int step;

    private float cpuUsageLow;


    public SimpleSwitcher(String paramFile, int checkerType, int schedulerType) {
        this.initUpTime = runtimeMXBean.getUptime();
        this.initCPUTime = threadMXBean.getCurrentThreadCpuTime();
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
        this.count = 0;
        parseParamFile(paramFile);
    }


    @Override
    public boolean isSwitch(int num) {
        boolean needSwitch = false;

        count++;

        if (count % step == 0) {
            switch (checkerType) {
                case CheckerType.ECC_TYPE: {
                    if (count != num) {
                        checkerType = CheckerType.CON_TYPE;
                        schedulerType = 1; //ImmedSched
                        needSwitch = true;
                        count = num + 1;
                    }
                    break;
                }

                case CheckerType.CON_TYPE: {
                    if (count != num) {
                        checkerType = CheckerType.PCC_TYPE;
                        schedulerType = 1;
                        needSwitch = true;
                        count = num + 1;
                    }
                    break;
                }

                case CheckerType.PCC_TYPE: {
                    if (count != num) {
                        checkerType = CheckerType.CONPCC_TYPE;
                        schedulerType = 1;
                        needSwitch = true;
                        count = num + 1;
                    }
                    break;
                }

                case CheckerType.CONPCC_TYPE: {
                    needSwitch = false;
                    break;
                }

            }
        }
        return needSwitch;
    }

    @Override
    public int getCheckerType() {
        return checkerType;
    }

    @Override
    public int getSchedulerType() {
        return schedulerType;
    }


    private float getCPURate() {
        int nrCPUs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        long elapsedCPUTime = threadMXBean.getCurrentThreadCpuTime() - initCPUTime;
        long elapsedUpTime = runtimeMXBean.getUptime() - initUpTime;

        float cpuRate = elapsedCPUTime * 100 / (elapsedUpTime * 1000000F * nrCPUs);

        return cpuRate;
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

        this.step = Integer.parseInt(properties.getProperty("step"));
        this.cpuUsageLow = Float.parseFloat(properties.getProperty("cpuUsageLow"));
    }
}
