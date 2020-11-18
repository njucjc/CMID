package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.util.Accuracy;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.TimestampHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder  extends AbstractCheckerBuilder implements Runnable{

    public CheckerBuilder() {
    }

    @Override
    public void run() {
        List<String> contextList;
        if("time".equals(this.changeHandlerType.split("-")[1])) {
            contextList = fileReader(this.dataFilePath);
        }
        else {
            contextList = fileReader(this.changeFilePath);
        }
        System.out.println("[INFO] 开始一致性检测......");


        dataCount = 0;
        totalTime = 0L;
        interval = 0L;
        while (dataCount < contextList.size()) {
            System.out.print("[INFO] 当前进度: " + (dataCount + 1) + "/" + contextList.size() + '\r');

            if (isFinished) {
                break;
            }

            if (!isPaused) {
                long startTime = System.nanoTime();

                String change = contextList.get(dataCount);
                changeHandler.doContextChange(dataCount, change);
                if (dataCount >= 1) {
                    interval = diff(contextList.get(dataCount), contextList.get(dataCount - 1));
                }
                dataCount++;

                long endTime = System.nanoTime(); //获取结束时间
                totalTime += (endTime - startTime);
            }

        }

        scheduler.reset();
        changeHandler.doCheck();

        int incCount = 0;
        for(Checker checker : checkerList) {
            incCount += checker.getInc();
        }

        System.out.println();
        System.out.println("[INFO] 一致性检测完毕......");
        LogFileHelper.getLogger().info("Total INC: " + incCount, true);
        LogFileHelper.getLogger().info("Total checking time: " + totalTime / 1000000 + " ms", true);

        accuracy(true);
        shutdown();

    }

    private long diff(String chg1, String chg2) {
        String [] elem1 = chg1.split(",");
        String [] elem2 = chg2.split(",");

        if (changeHandlerType.contains("time")) {
            return TimestampHelper.timestampDiff(elem1[0], elem2[0]);
        }
        else {
            return TimestampHelper.timestampDiff(elem1[3], elem2[3]);
        }
     }

    public static void main(String[] args) {

    }


}
