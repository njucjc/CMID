package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;

/**
 * Created by njucjc on 2017/10/7.
 */
public class BFuncHelper {


    public static boolean bfun(String name, Context context1, Context context2) {
        boolean value = false;
        switch (name) {

            default:
                System.out.println("[INFO] Illegal bfunc: " + name);
                System.exit(1);
                break;
        }
        return value;
    }

    public static void main(String[] args) {

    }
}
