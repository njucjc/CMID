package cn.edu.nju.switcher;


/**
 * Created by njucjc at 2018/7/24
 */
public interface Switcher {
    boolean isSwitch(int num, long i);

    int getCheckerType();

    int getSchedulerType();
}
