package cn.edu.nju.change;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.Scheduler;

import java.util.List;
import java.util.Map;

/**
 * Created by njucjc at 2018/1/23
 */
public abstract class ChangeHandler {
    protected Map<String, Pattern> patternMap;

    protected Map<String, Checker> checkerMap;

    protected Scheduler scheduler;

    protected List<Checker> checkerList;

    protected ContextParser contextParser;

    public ChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        this.patternMap = patternMap;
        this.checkerMap = checkerMap;
        this.scheduler = scheduler;
        this.checkerList = checkerList;
        this.contextParser = new ContextParser();
    }

    protected Context parseContext(int num, String change) {
        String [] strs = change.split(",");
        if(strs[0].equals("+") || strs[0].equals("-")) {
            int len = strs[0].length() + strs[1].length() + strs[2].length();
            return new Context(Integer.parseInt(strs[2]),
                    strs[3],
                    strs[4],
                    Double.parseDouble(strs[5]),
                    Double.parseDouble(strs[6]),
                    Double.parseDouble(strs[7]),
                    Integer.parseInt(strs[8]));
        }
        else {
            return contextParser.parseContext(num, change);
        }
    }
    public void doCheck() {
        boolean hasCheck = false;
        for(Checker checker : checkerList) {
            if(scheduler.schedule(checker.getName())) {
                boolean value = checker.doCheck();
                if (value) {
                    System.out.println("[rule] " + checker.getName() + ": Pass!");
                } else {
                    System.out.println("[rule] " + checker.getName() + ": Failed!");
                }
                hasCheck = true;
            }
        }
        if (hasCheck) {
            System.out.println("============================================================================================");
        }
    }

    protected final void deleteChange(String timestamp, String  patternId) {
        Pattern pattern = patternMap.get(patternId);
        Checker checker = checkerMap.get(patternId);
        checker.delete(patternId, timestamp);
     //   pattern.deleteFirstByTime(timestamp);
    }

    protected final void additionChange(String patternId, Context context) {
        Pattern pattern = patternMap.get(patternId);
        if(pattern.isBelong(context)) {
    //        pattern.addContext(context);
            Checker checker = checkerMap.get(pattern.getId());
            checker.add(pattern.getId(),context);
        }
    }

    public abstract void doContextChange(int num, String change);

    public void shutdown() {}



}
