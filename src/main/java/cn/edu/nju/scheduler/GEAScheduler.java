package cn.edu.nju.scheduler;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by njucjc at 2018/3/19
 */
public class GEAScheduler implements Scheduler{
    private List<Checker> checkerList;

    private Map<String, Boolean> scheduleMap;

    private Map<String, List<String>> currentBatchMap;

    protected ContextParser parser = new ContextParser();


    public GEAScheduler(List<Checker> checkerList) {
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
            if (matching(checker, change)) {
                scheduleMap.put(checker.getName(), true);
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

    private boolean matching(Checker checker, String change) {
        String [] elements = change.split(",");

        if (!checker.affected(elements[1])) {
            return false;
        }

        List<String> currentBatch = currentBatchMap.get(checker.getName());

        boolean result = sCondition(checker, currentBatch, elements);

        List<Boolean> subTree = calcSubTree(checker, elements[1], parser.parseChangeContext(elements));

        if (result) { //make batch empty
            currentBatch.clear();
        } else {
            int index = cCondition(checker, currentBatch, elements, subTree);
            if (index == -1) {
                currentBatch.add(change);
            }
            else {
                currentBatch.remove(index);
            }
        }

        return result;
    }

    private boolean sCondition(Checker checker, List<String> currentBatch, String [] elements) {
        boolean result = false;

        for(String c : currentBatch) {
            String [] e = c.split(",");
            if(checker.isInIncAddSet(e[0] + "," + e[1]) && checker.isInIncDelSet(elements[0] + "," + elements[1])) {
                result = true;
                break;
            }
        }

        return result;
    }

    protected int cCondition(Checker checker, List<String> currentBatch, String [] elements, List<Boolean> subTree) {
        return -1;
    }

    protected List<Boolean> calcSubTree(Checker checker, String patternId, Context c){
        List<Boolean> tmp = checker.calcSubTree(patternId, c);
        for(String c1 : currentBatchMap.get(checker.getName())) {
            List<Boolean> l1 = checker.calcSubTree(patternId, parser.parseChangeContext(c1.split(",")));
            int i;
            for (i = 0; i < tmp.size(); i++) {
                if (tmp.get(i) != l1.get(i)) {
                    break;
                }
            }

            if(i == tmp.size()) {
                return l1;
            }

        }
        return tmp;
    }

    @Override
    public void reset() {
        for(String key : scheduleMap.keySet()) {
            scheduleMap.put(key, true);
        }
    }

}
