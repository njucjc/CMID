package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.util.LogFileHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder  extends AbstractCheckerBuilder implements Runnable{

    public CheckerBuilder(String configFilePath) {
        super(configFilePath);
    }

//    @Override
    public void run() {
        List<String> contextList;
        if("time".equals(this.changeHandlerType.split("-")[1])) {
            contextList = fileReader(this.dataFilePath);
        }
        else {
            contextList = fileReader(this.changeFilePath);
        }
        int count = 0;
        long startTime = System.nanoTime();
        for(String change : contextList) {
            if(onDemand && switcher.isSwitch(computeWorkload())) {
                update(switcher.getCheckerType(), switcher.getSchedulerType());
            }

            changeHandler.doContextChange(count, change);
            count++;
        }
        scheduler.reset();
        changeHandler.doCheck();
        long endTime = System.nanoTime(); //获取结束时间
        int incCount = 0;
        int checkTimes = 0;
        long timeCount = 0L;
        for(Checker checker : checkerList) {
            incCount += checker.getInc();
            checkTimes += checker.getCheckTimes();
            timeCount = timeCount + checker.getTimeCount();
            LogFileHelper.getLogger().info(checker.getName() + ": " + checker.getInc() + " times" );
        }
        LogFileHelper.getLogger().info("Total INC: " + incCount + " times");
        LogFileHelper.getLogger().info("Total check: " + checkTimes + " times");
        LogFileHelper.getLogger().info("check time: " + timeCount / 1000000 + " ms");
        LogFileHelper.getLogger().info("run time: " + (endTime - startTime) / 1000000 + " ms");
        shutdown();
    }

    public static void main(String[] args) {
        CheckerBuilder checkerBuilder = new CheckerBuilder("config.properties");
    }


}
