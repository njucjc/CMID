package cn.edu.nju.change;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.util.TimestampHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by njucjc at 2018/1/23
 */
public class DynamicTimebasedChangeHandler extends ChangeHandler {

    private Set<String> timeTaskSet = ConcurrentHashMap.newKeySet();

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    public DynamicTimebasedChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        super(patternMap, checkerMap, scheduler, checkerList);
    }

    @Override
    protected void additionChange(String patternId, Context context) {
        Pattern p = patternMap.get(patternId);
        if(p.isBelong(context)) {
            String delTime = TimestampHelper.plusMillis(context.getTimestamp(), p.getFreshness());
            if(timeTaskSet.add(delTime)) {
                scheduledExecutorService.schedule(new ContextTimeoutTask(delTime), p.getFreshness(), TimeUnit.MILLISECONDS);
            }
            p.addContext(context);
            Checker checker = checkerMap.get(p.getId());
            checker.add(p.getId(),context);
        }
    }

    @Override
    public void doContextChange(int num, String change) {
        Context context = parseContext(num, change);
        context.setTimestamp(TimestampHelper.getCurrentTimestamp());
        for(String patternId : patternMap.keySet()) {
            additionChange(patternId, context);
        }
        scheduler.update();
        if(scheduler.schedule()) {
            doCheck();
        }
    }

    @Override
    public void shutdown() {
        while (!timeTaskSet.isEmpty());
        scheduledExecutorService.shutdown();
    }

    class ContextTimeoutTask extends TimerTask {
        private String timestamp;

        private ContextTimeoutTask(String timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public void run() {
            for(String patternId : patternMap.keySet()) {
                deleteChange(timestamp, patternId);
            }
            scheduler.update();
            if(scheduler.schedule()) {
                doCheck();
            }
            timeTaskSet.remove(timestamp);
        }
    }
}
