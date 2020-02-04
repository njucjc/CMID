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
    public static int timestampDiff(String timestamp1, String timestamp2) {
        int diff = Integer.parseInt(timestamp1) - Integer.parseInt(timestamp2);
        return diff > 0 ? diff : -diff;
    }



    public static int timestampCmp(String timestamp1, String timestamp2) {
        long diff = Integer.parseInt(timestamp1) - Integer.parseInt(timestamp2);
        if(diff < 0) {
            return 1;
        } else if(diff > 0) {
            return -1;
        } else {
            return 0;
        }
    }


    public static String plus(String timestamp, int freshness) {
        int n = Integer.parseInt(timestamp) + freshness;
        return n + "";
    }

    public static String getCurrentTimestamp() {
        return dfs.format(new java.util.Date());
    }
}
