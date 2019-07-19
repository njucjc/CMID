package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;

/**
 * Created by njucjc on 2017/10/7.
 */
public class BFuncHelper {

    private static boolean szElectricRange(Context context) {
        return Math.abs(context.getI()) <= 700.0;
    }

    private static boolean szVoltageRange(Context context) {
        return Math.abs(context.getV()) >= 1450.0 && Math.abs(context.getV()) <= 1800.0;
    }

    private static boolean szPowerRange(Context context) {
        return Math.abs(context.getP()) <= 4300.0;
    }

    private static boolean szSpeedRange(Context context) {
        return Math.abs(context.getV()) <= 75.0;
    }

    private static boolean szAccRange(Context context1, Context context2) {
        double t = (Math.abs(TimestampHelper.timestampDiff(context1.getTimestamp(),context2.getTimestamp())) / 1000.0);
        double v =  Math.abs(context1.getV() - context2.getV()) / 3.6;
        if (t == 0) {
            return true;
        }
        else {
            return v / t <= 1.2;
        }

    }


    public static boolean bfun(String name, Context context1, Context context2) {
        boolean value = false;
        switch (name) {
            case "sz_electric_range":
                value = BFuncHelper.szElectricRange(context1);
                break;
            case "sz_voltage_range":
                value = BFuncHelper.szVoltageRange(context1);
                break;
            case "sz_power_range":
                value = BFuncHelper.szPowerRange(context1);
                break;
            case "sz_speed_range":
                value = BFuncHelper.szSpeedRange(context1);
                break;
            case "sz_acc_range":
                value = BFuncHelper.szAccRange(context1, context2);
                break;
            default:
                assert  false:"[DEBUG] Illegal bfunc: " + name;
                break;
        }
        return value;
    }

    public static void main(String[] args) {
        ContextParser contextParser = new ContextParser();

    }
}
