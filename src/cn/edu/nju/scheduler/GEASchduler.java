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
        for(Checker checker : checkerList) {
            if(suspPairMatch(checker, change)) {
                scheduleMap.put(checker.getName(), true);
            }
        }
    }

    @Override
    public synchronized boolean schedule(String ruleName) {
        return scheduleMap.get(ruleName);
    }

    private boolean suspPairMatch(Checker checker, String change) {
        List<String> currentBatch = currentBatchMap.get(checker.getName());

        boolean result = false;
        String tmp = change.substring(0, change.indexOf(",", 2));

        for(String c : currentBatch) {
            if(checker.isInIncAddSet(c) && checker.isInIncDelSet(tmp)) {
                result = true;
                break;
            }
        }

        if (result) { //make batch empty
            currentBatch.clear();
        }
        currentBatch.add(tmp);

        return result;
    }

}
