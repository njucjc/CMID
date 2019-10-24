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

    private String type; // change handler type

    private int count; // context id counter

    private int step; // context interleave check step

    private long interval; // context interval

    private long sum; // the sum of interval

    private int stepCount; // step counter


    public SimpleSwitcher(String type, String paramFile, int checkerType, int schedulerType) {
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
        this.type = type;
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
                    schedulerType = 1;
                    needSwitch = true;
                    count = num;

                    reset();
                }
                else {
                    if (schedulerType == 0 && isWorkloadLow(i)) {
                        checkerType = CheckerType.PCC_TYPE;
                        schedulerType = 1;
                        needSwitch = true;
                    }

                }
                break;
            }

            case CheckerType.CONPCC_TYPE: {
                if (count != num) {
                    if (type.contains("change")) { //change-based data
                        checkerType = CheckerType.PCC_TYPE;
                        schedulerType = 0;
                        needSwitch = true;
                        count = num;
                    }

                    reset();
                }
                else {
                   if (isWorkloadLow(i)) {
                       checkerType = CheckerType.PCC_TYPE;
                       schedulerType = 1;
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
