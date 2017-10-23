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
    public static long timestampDiff(String timestamp1, String timestamp2) {
        long diff = 0;
        try {
            java.util.Date begin = dfs.parse(timestamp1);
            java.util.Date end = dfs.parse(timestamp2);
            diff = end.getTime() - begin.getTime();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return diff > 0 ? diff : -diff;
    }

    public static void main(String[] args) {
        System.out.println(timestampDiff("2007-10-26 11:00:00:000","2007-10-26 11:00:00:057"));
        System.out.println(timestampDiff("2007-10-26 11:00:00:000","2007-10-26 11:00:00:228"));
        System.out.println(timestampDiff("2007-10-26 11:00:00:057","2007-10-26 11:00:00:228"));
    }
}
