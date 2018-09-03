package cn.edu.nju.switcher;


import cn.edu.nju.checker.CheckerType;

/**
 * Created by njucjc at 2018/7/24
 */
public class SimpleSwitcher implements Switcher {

    private final static int THRESHOLD = 400;

    private final static int STEP = 5;

    private int checkerType;

    private int schedulerType;

    private int count;

    private int totalDelay;

    public SimpleSwitcher(int checkerType, int schedulerType) {
        this.checkerType = checkerType;
        this.schedulerType = schedulerType;
        this.count = 0;
        this.totalDelay = 0;
    }

    @Override
    public synchronized boolean isSwitch(long delay) {
        boolean needSwitch = false;

        count = (count + 1) % STEP;
        totalDelay += delay;

        if(count == 0) {

            switch (checkerType) {
                case CheckerType.ECC_TYPE: {
                    if (totalDelay / STEP > THRESHOLD) {
                        needSwitch = true;
                        checkerType = CheckerType.CON_TYPE;
                    }
                    break;
                }

                case CheckerType.CON_TYPE: {
                    if (totalDelay / STEP > THRESHOLD) {
                        needSwitch = true;
                        checkerType = CheckerType.PCC_TYPE;
                    }
                    break;
                }

            }
            totalDelay = 0;
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
