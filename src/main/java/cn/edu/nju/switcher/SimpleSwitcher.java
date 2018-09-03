package cn.edu.nju.switcher;


import cn.edu.nju.checker.CheckerType;

/**
 * Created by njucjc at 2018/7/24
 */
public class SimpleSwitcher implements Switcher {

    private int checkerType;

    private int schedulerType;

 //   private int count;

    public SimpleSwitcher(int checkerType, int schedulerType) {
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
    //    this.count = 0;
    }

    @Override
    public synchronized boolean isSwitch(long delay) {
        boolean needSwitch = false;

        switch (checkerType) {
            case CheckerType.ECC_TYPE: {
                if(delay> 300) {
                    needSwitch = true;
                    checkerType = CheckerType.CON_TYPE;
                }
                break;
            }

            case CheckerType.CON_TYPE: {
                if(delay > 300) {
                    needSwitch = true;
                    checkerType = CheckerType.PCC_TYPE;
                }
                break;
            }

        }
        return needSwitch;
    }

    @Override
    public synchronized int getCheckerType() {
        return checkerType;
    }

    @Override
    public synchronized int getSchedulerType() {
        return schedulerType;
    }
}
