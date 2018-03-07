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

    @Override
    public void run() {
        List<String> contextList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(contextFilePath);
            BufferedReader bufferedReader = new BufferedReader(fr);

            String change;
            while ((change = bufferedReader.readLine()) != null) {
                contextList.add(change);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int count = 0;
        long startTime = System.nanoTime();
        for(String change : contextList) {
            changeHandler.doContextChange(count, change);
            count++;
        }
        long endTime = System.nanoTime(); //获取结束时间
        int incCount = 0;
        int checkTimes = 0;
        for(Checker checker : checkerList) {
            incCount += checker.getInc();
            checkTimes += checker.getCheckTimes();
            LogFileHelper.getLogger().info(checker.getName() + ": " + checker.getInc() + " times" );
        }
        LogFileHelper.getLogger().info("Total INC: " + incCount + " times");
        LogFileHelper.getLogger().info("Total check: " + checkTimes + " times");
        LogFileHelper.getLogger().info("run time： " + (endTime - startTime) / 1000000 + " ms");
        shutdown();
    }

    public static void main(String[] args) {
        CheckerBuilder checkerBuilder = new CheckerBuilder("resource/config.properties");
    }


}
