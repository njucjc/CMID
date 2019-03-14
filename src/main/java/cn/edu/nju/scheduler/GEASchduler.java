package cn.edu.nju.scheduler;

import cn.edu.nju.checker.Checker;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by njucjc at 2018/3/19
 */
public class GEASchduler implements Scheduler{
    private List<Checker> checkerList;

    private Map<String, Boolean> scheduleMap;

    private Map<String, List<String>> currentBatchMap;

//     for(String key : stMap.keySet()) {
//        cctMap.put(key, new CopyOnWriteArrayList<>());
//    }


    public GEASchduler(List<Checker> checkerList) {
        this.checkerList = checkerList;
        this.scheduleMap = new ConcurrentHashMap<>();
        this.currentBatchMap = new ConcurrentHashMap<>();


        for(Checker checker : checkerList) {
            String name = checker.getName();
            scheduleMap.put(name, false);
            currentBatchMap.put(name, new CopyOnWriteArrayList<>());
        }
    }

    @Override
    public synchronized void update(String change) {
        String [] elements = change.split(",");

        for(Checker checker : checkerList) {
            if(checker.affected(elements[1])) {
                if (suspPairMatch(checker, elements[0] + "," + elements[1])) {
                    scheduleMap.put(checker.getName(), true);
                }
            }
        }
    }

    @Override
    public synchronized boolean schedule(String ruleName) {
        boolean result = scheduleMap.get(ruleName);
        if(result) {
            scheduleMap.put(ruleName, false);
        }
        return result;
    }

    private boolean suspPairMatch(Checker checker, String change) {
        List<String> currentBatch = currentBatchMap.get(checker.getName());

        boolean result = false;
       // String tmp = change.substring(0, change.indexOf(",", 2));

        for(String c : currentBatch) {
            if(checker.isInIncAddSet(c) && checker.isInIncDelSet(change)) {
                result = true;
                break;
            }
        }

        if (result) { //make batch empty
            currentBatch.clear();
        }
        currentBatch.add(change);

        return result;
    }

    @Override
    public void reset() {
        for(String key : scheduleMap.keySet()) {
            scheduleMap.put(key, true);
        }
    }

}
