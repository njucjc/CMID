package cn.edu.nju.switcher;


import cn.edu.nju.checker.CheckerType;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by njucjc at 2018/7/24
 */
public class SimpleSwitcher implements Switcher {

//    private final static int THRESHOLD = 70;
//
//    private final static int STEP = 5;
    private int maxDelay;

    private  int step;

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

            }
            totalDelay = 0;
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
