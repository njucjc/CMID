package cn.edu.nju.scheduler;

/**
 * Created by njucjc on 2017/10/23.
 */
public class BatchScheduler implements Scheduler{
    private int batch; //批处理数
    private int count; //计数

    public BatchScheduler(int batch) {
        this.batch = batch;
        this.count = 0;
    }

    @Override
    public synchronized void update(String change) {
        count = (count + 1) % batch;
    }

    @Override
    public synchronized boolean schedule(String ruleName) {
        return count == 0;
    }

    @Override
    public void reset() {
        count = 0;
    }
}
