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
public class CheckerBuilder  extends AbstractCheckerBuilder{

    public CheckerBuilder() {
        reset();
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


        String timestamp1 = null;
        String timestamp2 = null;

        long sum1 = 0;
        long sum2 = 0;

        long count1 = 0;
        long count2 = 0;
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

                    sum1 += interval;
                    sum2 += interval;
                }
                else { // 初始化
                    timestamp1 = getTimestamp(contextList.get(0));
                    timestamp2 = timestamp1;
                }
                dataCount++;
                progress = ((double) dataCount) / contextList.size();

                count1++;
                count2++;

                // 最近500 ms的平均间隔
                if (TimestampHelper.timestampDiff(timestamp1, getTimestamp(contextList.get(dataCount))) >= 500) {
                    avgInterval1 = sum1 / count1;
                    sum1 = 0;
                    count1 = 0;
                    timestamp1 = getTimestamp(contextList.get(dataCount));
                }

                // 最近1000 ms的平均间隔
                if (TimestampHelper.timestampDiff(timestamp2, getTimestamp(contextList.get(dataCount))) >= 1000) {
                    avgInterval2 = sum2 / count2;
                    sum2 = 0;
                    count2 = 0;
                    timestamp2 = getTimestamp(contextList.get(dataCount));
                }

                long endTime = System.nanoTime(); //获取结束时间
                totalTime += (endTime - startTime) ;
            }

        }

        if (!isFinished) {
            progress = 1.0;
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
          String timestamp1 = getTimestamp(chg1);
          String timestamp2 = getTimestamp(chg2);
          return TimestampHelper.timestampDiff(timestamp1, timestamp2);
     }

    public static void main(String[] args) {

    }


}
