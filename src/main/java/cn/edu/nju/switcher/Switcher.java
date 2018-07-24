package cn.edu.nju.switcher;

import cn.edu.nju.context.Context;

/**
 * Created by njucjc at 2018/7/24
 */
public interface Switcher {
    boolean isSwitch(String context);

    int getCheckerType();

    int getSchedulerType();
}
