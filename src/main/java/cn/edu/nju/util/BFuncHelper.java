package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.Param;

import java.util.List;

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

    public static boolean bfunc(String name, List<Param> list1, List<Context> list2) {
        Context [] contexts = new Context[4]; // 下标为0的没有用
        for (int i = 0; i < list1.size(); i++) {
            Param p = list1.get(i);
            if (!p.getDefaultValue().equals("")) continue; // 有默认值的参数不是Context对象
            if (p.getOp().equals("")) {
                contexts[p.getPos()] = list2.get(i); //
            }
            else {
                if ("get_oppo".equals(p.getOp())) { // 参数操作
                    contexts[p.getPos()] = new Context(
                            list2.get(i).getId(),
                            list2.get(i).getTimestamp(),
                            TrafficGraph.getOppo(list2.get(i).getCode()),
                            list2.get(i).getType()
                    );
                }
            }
        }
        // 计算真值
        boolean value = false;
        switch (name) {
            case "before":
                value = before(contexts[1], contexts[2]);
                break;
            case "gate":
                value = gate(contexts[1], contexts[2]);
                break;
            case "equal":
                value = equal(contexts[1], contexts[2]);
                break;
            case "conn":
                value = conn(contexts[1], contexts[2], Integer.parseInt(list1.get(list1.size() - 1).getDefaultValue()));
                break;
            case "oppo":
                value = oppo(contexts[1], contexts[2]);
                break;
            default:
                assert  false:"[DEBUG] Illegal bfunc: " + name;
                break;
        }
        return value;
    }
}
