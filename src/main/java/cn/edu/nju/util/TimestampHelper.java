package cn.edu.nju.util;

import java.text.SimpleDateFormat;


/**
 * Created by njucjc on 2017/10/23.
 */
public class TimestampHelper {
    private static SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    /**
     *返回两个时间戳对应时间差的毫秒数
     * @param timestamp1
     * @param timestamp2
     * @return 两个时间戳的时间差
     */
    public static long timestampDiff(long timestamp1, long timestamp2) {
        long diff = getDiff(timestamp1, timestamp2);
        return diff > 0 ? diff : -diff;
    }

    private static long getDiff(long timestamp1, long timestamp2) {

        return timestamp2 - timestamp1;
    }

    public static int timestampCmp(long timestamp1, long timestamp2) {
        long diff = getDiff(timestamp1, timestamp2);
        if(diff < 0) {
            return 1;
        } else if(diff > 0) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 计算timstamp加上一个毫秒数的timestamp
     * @param timestamp
     * @param millis
     * @return
     */
    public static long plusMillis(long timestamp, long millis) {
        return timestamp + millis;
    }

    public static long getCurrentTimestamp() {
        return new java.util.Date().getTime();
    }

    public static java.util.Date parserDate(String time) {
        java.util.Date date = null;
        try {
            date = dfs.parse(time);
        } catch (Exception e) {
            System.out.println("[INFO] 时间戳格式错误");
            System.exit(1);
        }
        return date;
    }


    public static void main(String[] args) {
    }
}
