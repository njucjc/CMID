package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.checker.EccChecker;
import cn.edu.nju.checker.PccChecker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextRepoService;
import cn.edu.nju.context.ContextStaticRepo;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.BatchScheduler;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.util.LogFileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder extends AbstractCheckerBuilder {

    public CheckerBuilder(String configFilePath) {
        super(configFilePath);
    }

    public void run() {
        Context context;
        try {
            long startTime = System.nanoTime();
            while ( (context =  contextRepoService.getContext()) != null) {
                doContextChange(context);
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
    }

    private void doContextChange(Context context) {
        Set<String> timeSet = new TreeSet<>();
        //按升序收集过期时间
        for(String key : patternMap.keySet()) {
            Pattern pattern = patternMap.get(key);
            timeSet.addAll(pattern.getOutOfDateTimes(context.getTimestamp()));
        }

        //按时间顺序删除context
        for(String timestamp : timeSet) {
            for(String key : patternMap.keySet()) {
                Pattern pattern = patternMap.get(key);
                pattern.deleteFirstByTime(timestamp);
                Checker checker = checkerMap.get(pattern.getId());
                checker.delete(pattern.getId(), timestamp);
            }
            scheduler.update();
            if(scheduler.schedule()) {
                doCheck();
            }
        }

        //在相关的pattern里添加context
        System.out.println("[DEBUG] '+' " + context.toString());
        for(String key : patternMap.keySet()) {
            Pattern pattern = patternMap.get(key);
            if(pattern.isBelong(context)) {
                pattern.addContext(context);
                Checker checker = checkerMap.get(pattern.getId());
                checker.add(pattern.getId(),context);
            }
        }
        scheduler.update();
        if(scheduler.schedule()) {
            doCheck();
        }
    }



    public static void main(String[] args) {
        CheckerBuilder checkerBuilder = new CheckerBuilder("resource/config.properties");
    }


}
