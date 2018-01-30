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
 * Created by njucjc at 2018/1/30
 */
public class DynamicChangebasedChangeHandler extends ChangeHandler {

    private Set<String> timeTaskSet = ConcurrentHashMap.newKeySet();

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    public DynamicChangebasedChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        super(patternMap, checkerMap, scheduler, checkerList);
    }

    @Override
    public void doContextChange(int num, String change) {
        Context context = parseContext(num, change);
        String [] strs = change.split(",");

        String op = strs[0];
        String patternId = strs[1];

        if(op.equals("+")) {
            Pattern p = patternMap.get(patternId);
            String deleteTime = TimestampHelper.plusMillis(context.getTimestamp(), p.getFreshness());
            if(timeTaskSet.add(deleteTime + "," + patternId)) {
                scheduledExecutorService.schedule(new ContextTimeoutTask(deleteTime, patternId), p.getFreshness(), TimeUnit.MILLISECONDS);
            }
            additionChange(patternId, context);
            scheduler.update();
            if (scheduler.schedule()) {
                doCheck();
            }
        }
    }

    @Override
    public void shutdown() {
        while (!timeTaskSet.isEmpty());
        scheduledExecutorService.shutdown();
    }

    class ContextTimeoutTask extends TimerTask {
        private String timestamp;

        private String patternId;

        private ContextTimeoutTask(String timestamp, String patternId) {
            this.timestamp = timestamp;
            this.patternId = patternId;
        }
        @Override
        public void run() {
            deleteChange(timestamp, patternId);
            scheduler.update();
            if(scheduler.schedule()) {
                doCheck();
            }
            timeTaskSet.remove(timestamp + "," + patternId);
        }
    }
}
