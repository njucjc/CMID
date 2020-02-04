package cn.edu.nju.util;

import cn.edu.nju.context.Context;

/**
 * Created by njucjc at 2020/2/3
 */
public class BFuncHelper {
    public static boolean before(Context c1, Context c2) {
        return c1.getId() < c2.getId();
    }

    public static boolean gate(Context c1, Context c2) {
        return c1.getType() == 3 || c2.getType() == 3;
    }

    public static boolean equal(Context c1, Context c2) {
        return (c1.getCode()).equals(c2.getCode());
    }

    public static boolean conn(Context c1, Context c2, int k) {
        if (TrafficGraph.getPath(c1.getCode(), c2.getCode(), k) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean oppo(Context c1, Context c2) {
        String oppoCode = TrafficGraph.getOppo(c1.getCode());
        return c2.getCode().equals(oppoCode);
    }

    public static boolean bfunc(String name, Context c1, Context c2) {
        return true;
    }
}
