package cn.edu.nju.change;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.context.Context;
import java.util.List;
import java.util.Map;

/**
 * Created by njucjc at 2018/1/23
 */
public class StaticChangebasedChangeHandler extends ChangeHandler {
    public StaticChangebasedChangeHandler(Map<String, Pattern> patternMap, Map<String, Checker> checkerMap, Scheduler scheduler, List<Checker> checkerList) {
        super(patternMap, checkerMap, scheduler, checkerList);
    }

    @Override
    public void doContextChange(int num, String change) {
        Context context = parseContext(num, change);
        String [] strs = change.split(",");

        String op = strs[0];
        String patternId = strs[1];

        if (op.equals("+")) {
            additionChange(patternId, context);
        }
        else {
            deleteChange(context.getTimestamp(), patternId);
        }
        scheduler.update();
        if(scheduler.schedule()) {
            doCheck();
        }
    }
}
