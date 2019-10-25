package cn.edu.nju.switcher;


import cn.edu.nju.checker.CheckerType;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by njucjc at 2018/7/24
 */
public class SimpleSwitcher implements Switcher {


    private int checkerType;

    private int schedulerType;

    private int count; // context id counter

    private int step; // context interleave check step

    private long interval; // context interval

    private long sum; // the sum of interval

    private int stepCount; // step counter


    public SimpleSwitcher(String paramFile, int checkerType, int schedulerType) {
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
        this.count = 0;
        parseParamFile(paramFile);
        this.sum = 0;
        this.stepCount = 0;
    }

    private void reset() {
        this.stepCount = 0;
        this.sum = 0;
    }

    private boolean isWorkloadLow(long i) {
        boolean res = false;
        this.stepCount++;
        this.sum += i;
        if (this.stepCount == step) {
            if (this.sum / this.step >= this.interval) {
                res = true;
            }
            reset();
        }
        return res;
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
        this.interval = Integer.parseInt(properties.getProperty("interval"));
    }


    @Override
    public boolean isSwitch(int num, long i) {
        boolean needSwitch = false;

        switch (checkerType) {
            case CheckerType.PCC_TYPE: {
                if (count != num) {
                    checkerType = CheckerType.CONPCC_TYPE;
                    needSwitch = true;

                    count = num;
                    reset();
                }
                break;
            }

            case CheckerType.CONPCC_TYPE: {
                if (count != num) {
                    count = num;
                    reset();
                }
                else {
                   if (isWorkloadLow(i)) {
                       checkerType = CheckerType.PCC_TYPE;
                       needSwitch = true;
                   }
                }
                break;
            }
        }

        count++;

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

}
