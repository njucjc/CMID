package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.util.ChangeHelper;
import cn.edu.nju.util.LogFileHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder  extends AbstractCheckerBuilder implements Runnable{

    public CheckerBuilder(String configFilePath) {
        super(configFilePath);
    }

    @Override
    public void run() {
        List<String> contextList = fileReader(this.dataFilePath);
        System.out.println("[INFO] 开始一致性处理");
        int count = 0;
        long timeSum = 0L;

        Map<Long, List<String>> deleteChanges = new TreeMap<>();

        for(String line : contextList) {
            System.out.print("[INFO] 当前进度: " + (count + 1) + "/" + contextList.size() + '\r');

            Context context = ContextParser.jsonToContext(count, line);
            List<String> changes = ChangeHelper.toChanges(context, deleteChanges, patternList);

            long startTime = System.nanoTime();
            for (String chg : changes) {
                changeHandler.doContextChange(chg);
            }
            long endTime = System.nanoTime(); //获取结束时间
            timeSum += (endTime - startTime);

            count++;
        }

        long start = System.nanoTime();
        Iterator<Map.Entry<Long, List<String>>> it = deleteChanges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, List<String>> entry = it.next();
            long key = entry.getKey();
            for (String chg : deleteChanges.get(key)) {
                changeHandler.doContextChange(chg);
            }
        }
        long end = System.nanoTime();
        timeSum += (end - start);


        int incCount = 0;
        for(Checker checker : checkerList) {
            incCount += checker.getInc();
        }

        System.out.println();
        LogFileHelper.getLogger().info("[INFO] Total INC: " + incCount, true);
        LogFileHelper.getLogger().info("[INFO] Total checking time: " + timeSum / 1000000 + " ms", true);

        accuracy(LogFileHelper.logFilePath);
        shutdown();

    }


}
