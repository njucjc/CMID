package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;

/**
 * Created by njucjc on 2017/10/7.
 */
public class BFuncHelper {
    /**
     * 所有车辆经度在[112, 116]，纬度在[20, 24]
     * @param context
     * @return
     */
    public static boolean szLocRange(Context context) {
//        double longitude = context.getLongitude();
//        double latitude = context.getLatitude();
//
//        return  Double.compare(longitude, 112.0) >= 0 &&
//                Double.compare(longitude, 116.0) <= 0 &&
//                Double.compare(latitude, 20.0) >= 0   &&
//                Double.compare(latitude,24.0) <= 0;
        return true;
    }

    /**
     * 是否为同一辆车
     * @param context1
     * @param context2
     * @return
     */
    private static boolean same(Context context1, Context context2) {
        return context1.equals(context2);
    }


    /**
     * 两车速度相近
     * @param context1
     * @param context2
     * @return
     */
    private static boolean szSpdClose(Context context1, Context context2) {
        double speed1 = context1.getSpeed();
        double speed2 = context2.getSpeed();
        return  Double.compare(speed1 - speed2, -50.0) >= 0 &&
                Double.compare(speed1 - speed2, 50.0) <= 0;
    }

    /**
     * 两次检测到的距离相差不能过远
     * @param context1
     * @param context2
     * @return
     */
    private static boolean szLocDist(Context context1, Context context2) {
        double d = getDistance(context1, context2);
        return Double.compare(d, 0.025) <= 0;

    }

    /**
     * 两次检测到的距离相差不能过远也不可能为0
     * @param context1
     * @param context2
     * @return
     */
    private static boolean szLocDistNeq(Context context1, Context context2) {
        double d = getDistance(context1, context2);
        return  Double.compare(d, 0.025) <= 0 &&
                Double.compare(d, 0.0) != 0;
    }

    /**
     * 两车位置相近
     * @param context1
     * @param context2
     * @return
     */
    private static boolean szLocClose(Context context1, Context context2) {
        double d = getDistance(context1, context2);
        return Double.compare(d, 0.001) <= 0;
    }

    /**
     * 计算两车距离
     * @param context1
     * @param context2
     * @return
     */
    private static double getDistance(Context context1, Context context2) {
        /*double longitude1 = context1.getLongitude();
        double latitude1 = context1.getLatitude();
        double longitude2 = context2.getLongitude();
        double latitude2 = context2.getLatitude();

        return LocationHelper.getDistance(longitude1, latitude1, longitude2, latitude2);*/
        return 0.0;
    }

    private static boolean szElectricRange(Context context) {
        return Math.abs(context.getI()) <= 700.0;
    }

    private static boolean szVoltageRange(Context context) {
        return Math.abs(context.getV()) >= 1450.0 && Math.abs(context.getV()) <= 1800.0;
    }

    private static boolean szPowerRange(Context context) {
        return Math.abs(context.getPower()) <= 4300.0;
    }

    private static boolean szSpeedRange(Context context) {
        return Math.abs(context.getSpeed()) <= 75.0;
    }

    private static boolean szAccRange(Context context1, Context context2) {
        double t = (Math.abs(TimestampHelper.timestampDiff(context1.getTimestamp(),context2.getTimestamp())) / 1000.0);
        double v =  Math.abs(context1.getSpeed() - context2.getSpeed()) / 3.6;
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
            case "sz_loc_range":
                value = BFuncHelper.szLocRange(context1);
                break;
            case "same":
                value = BFuncHelper.same(context1, context2);
                break;
            case "sz_loc_close":
                value = BFuncHelper.szLocClose(context1, context2);
                break;
            case "sz_spd_close":
                value = BFuncHelper.szSpdClose(context1, context2);
                break;
            case "sz_loc_dist":
                value = BFuncHelper.szLocDist(context1, context2);
                break;
            case "sz_loc_dist_neq":
                value = BFuncHelper.szLocDistNeq(context1, context2);
                break;
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
        boolean loc = szLocClose(contextParser.parseContext(0,"2007-10-26 11:00:34:240,A,12198,113.883050,22.579217,70,-1,1,-1"),
                contextParser.parseContext(0,"2007-10-26 11:00:25:064,A,12198,113.883467,22.578850,17,-1,1,-1")
                );

        boolean spd = szSpdClose(contextParser.parseContext(0,"2007-10-26 11:00:34:240,A,12198,113.883050,22.579217,70,-1,1,-1"),
                contextParser.parseContext(0,"2007-10-26 11:00:25:064,A,12198,113.883467,22.578850,17,-1,1,-1")
             );

        System.out.println(loc + " " + spd);

    }
}
