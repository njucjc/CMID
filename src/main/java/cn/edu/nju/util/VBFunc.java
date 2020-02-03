package cn.edu.nju.util;

import cn.edu.nju.context.VContext;

/**
 * Created by njucjc at 2020/2/3
 */
public class VBFunc {
    public static boolean before(VContext c1, VContext c2) {
        return c1.getId() < c2.getId();
    }

    public static boolean gate(VContext c1, VContext c2) {
        return c1.getType() == 3 || c2.getType() == 3;
    }

    public static boolean equal(VContext c1, VContext c2) {
        return (c1.getCode()).equals(c2.getCode());
    }

    public static boolean conn(VContext c1, VContext c2, int k) {
        if (TrafficGraph.getPath(c1.getCode(), c2.getCode(), k) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean oppo(VContext c1, VContext c2) {
        String oppoCode = TrafficGraph.getOppo(c1.getCode());
        return c2.getCode().equals(oppoCode);
    }
}
