package cn.edu.nju.switcher;


/**
 * Created by njucjc at 2018/7/24
 */
public interface Switcher {
    boolean isSwitch(long delay);

    int getCheckerType();

    int getSchedulerType();
}
