package cn.edu.nju.scheduler;

/**
 * Created by njucjc on 2017/10/23.
 */
public class BatchSchedule implements Scheduler{
    private int batch; //批处理数
    private int count; //计数

    public BatchSchedule(int batch) {
        this.batch = batch;
        this.count = 0;
    }

    @Override
    public  void update() {
        count = (count + 1) % batch;
    }

    @Override
    public boolean schedule() {
        return count == 0;
    }
}
