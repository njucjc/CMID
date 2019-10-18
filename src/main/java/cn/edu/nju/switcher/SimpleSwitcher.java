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

    private int checkerType;

    private int schedulerType;

    private int count;


    public SimpleSwitcher(int checkerType, int schedulerType) {
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
        this.count = -1;
    }


    @Override
    public boolean isSwitch(int num) {
        boolean needSwitch = false;
        if (count + 1 != num) {
            needSwitch = true;
        }
        count++;

        if (needSwitch) {

            count = num;
            switch (checkerType) {
                case CheckerType.ECC_TYPE: {
                    checkerType = CheckerType.CON_TYPE;
                    break;
                }

                case CheckerType.CON_TYPE: {
                    checkerType = CheckerType.PCC_TYPE;
                }

                case CheckerType.PCC_TYPE: {
                    checkerType = CheckerType.CONPCC_TYPE;
                }

                case CheckerType.CONPCC_TYPE: {
                    needSwitch = false;
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
}
