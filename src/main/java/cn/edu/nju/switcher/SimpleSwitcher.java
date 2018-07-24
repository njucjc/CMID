package cn.edu.nju.switcher;

import cn.edu.nju.context.Context;

/**
 * Created by njucjc at 2018/7/24
 */
public class SimpleSwitcher implements Switcher {

    private int batch;

    private int count;

    private  java.util.Random r = new java.util.Random();

    public SimpleSwitcher(int batch) {
        this.batch = batch;
        this.count = 0;
    }

    @Override
    public boolean isSwitch(String context) {
        count = (count + 1) % batch;
        return count == 0;
    }

    @Override
    public int getCheckerType() {
        return r.nextInt(2);
    }

    @Override
    public int getSchedulerType() {
        return r.nextInt(2);//0:GEAS 1:ImmedSched
    }
}
