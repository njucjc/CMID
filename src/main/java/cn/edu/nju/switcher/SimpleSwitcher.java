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

    private int count;

    private int step;

    private int interleave;

    private String type;


    public SimpleSwitcher(String type, String paramFile, int checkerType, int schedulerType) {
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
        this.type = type;
        this.count = 0;
        parseParamFile(paramFile);
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
        this.interleave = Integer.parseInt(properties.getProperty("interleave"));
    }


    @Override
    public boolean isSwitch(int num) {
        boolean needSwitch = false;

        switch (checkerType) {
            case CheckerType.PCC_TYPE: {
                if (count != num) {
                    checkerType = CheckerType.CONPCC_TYPE;
                    schedulerType = 1;
                    needSwitch = true;
                    count = num;
                }
                else {
                    //TODO
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
                }
                else {
                   //TODO
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
