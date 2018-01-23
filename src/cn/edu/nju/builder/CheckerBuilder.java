package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.util.LogFileHelper;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder  extends AbstractCheckerBuilder implements Runnable{

    public CheckerBuilder(String configFilePath) {
        super(configFilePath);
    }

    @Override
    public void run() {
        try {
            FileReader fr = new FileReader(contextFilePath);
            BufferedReader bufferedReader = new BufferedReader(fr);

            int count = 0;
            String change;
            long startTime = System.nanoTime();
            while ((change = bufferedReader.readLine()) != null) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        shutdown();
    }

    public static void main(String[] args) {
        CheckerBuilder checkerBuilder = new CheckerBuilder("resource/config.properties");
    }


}
