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

    private String schedulerName;

    private String checkerName;

    public long timeCount = 0L;

    public ChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        this.patternMap = patternMap;
        this.checkerMap = checkerMap;
        this.scheduler = scheduler;
        this.checkerList = checkerList;
        this.contextParser = new ContextParser();
        this.schedulerName = getClassString(scheduler.getClass().toString());
        this.checkerName = getClassString(checkerList.get(0).getClass().toString());
    }

    private String getClassString(String str) {
        return str.substring(str.lastIndexOf(".") + 1);
    }

    protected Context parseContext(int num, String change) {
        String [] strs = change.split(",");
        if(strs[0].equals("+") || strs[0].equals("-")) {
            int len = strs[0].length() + strs[1].length() + strs[2].length();
            return new Context(Integer.parseInt(strs[2]),
                    strs[3],
                    Double.parseDouble(strs[4]),
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
        long start = System.nanoTime();
        boolean hasCheck = false;
        for(Checker checker : checkerList) {
            if(scheduler.schedule(checker.getName())) {
                boolean value = checker.doCheck();
                if (value) {
                    System.out.println("[" + checkerName + " + " + schedulerName + "] " + checker.getName() + ": Pass!");
                } else {
                    System.out.println("[" + checkerName + " + " + schedulerName + "] " + checker.getName() + ": Failed!");
                }
                hasCheck = true;
            }
        }
        if (hasCheck) {
            System.out.println("============================================================================================");
        }
        long end = System.nanoTime();
        timeCount += (end -start);
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

    public synchronized void update(Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        this.checkerMap = checkerMap;
        this.scheduler = scheduler;
        this.checkerList = checkerList;
        this.checkerName = getClassString(checkerList.get(0).getClass().toString());
        this.schedulerName = getClassString(scheduler.getClass().toString());
    }

}
