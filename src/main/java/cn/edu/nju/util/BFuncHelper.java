package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.context.State;

/**
 * Created by njucjc on 2017/10/7.
 */
public class BFuncHelper {

//    private static boolean nextMoment(Context c1, Context c2) {
//        return c2.getId() - c1.getId() == 1;
//    }

    private static Context now(Context c1, Context c2) {
        if (c1.getId() - c2.getId() == 1) {
            return c2;
        }
        else if (c2.getId() - c1.getId() == 1){
            return c1;
        }
        else {
            return null;
        }
    }

    private static Context next(Context c1, Context c2) {
        if (c1.getId() - c2.getId() == 1) {
            return c1;
        }
        else if (c2.getId() - c1.getId() == 1){
            return c2;
        }
        else {
            return null;
        }
    }

    private static boolean electricRange(Context context) {
        return Math.abs(context.getI()) <= 740.0;
    }

    private static boolean voltageRange(Context context) {
        return Math.abs(context.getU()) >= 1450.0 && Math.abs(context.getU()) <= 1800.0;
    }

    private static boolean accRange(Context context) {
        return Math.abs(context.getA()) <= 1.0;
    }

    private static boolean accRateRange(Context c1, Context c2) {

        boolean res = true;
        double t = (Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(),c2.getTimestamp())) / 1000.0);
        double a = Math.abs(c1.getA() - c2.getA());

        if (t != 0 && now(c1, c2) != null) {
            res = (a / t) <= 1.5;
        }
        return res;
    }

    private static boolean maxSpeed(Context c1, Context c2) {
        double max = 75.0;
        Context c = now(c1, c2);
        if (c == null) {
            return false;
        }
        if (c.getStatus() == State.COAST) {
            return Math.abs(c.getV()) >= (max * 1.1);
        }
        else {
            return false;
        }
    }

    private static boolean minSpeed(Context c1, Context c2) {
        double min = 25.0;
        Context c = now(c1, c2);
        if (c == null) {
            return false;
        }
        if (c.getStatus() == State.COAST) {
            return Math.abs(c.getV()) <= (min * 0.9);
        }
        else {
            return false;
        }
    }

    private static boolean transToBrake(Context c1, Context c2) {
        Context c = next(c1, c2);
        if(c != null) {
            return c.getStatus() == State.BRAKE;
        }
        return true;
    }

    private static boolean transToTraction(Context c1, Context c2) {
        Context c = next(c1, c2);
        if(c != null) {
            return c.getStatus() == State.TRACTION;
        }
        return true;
    }

    private static boolean inTractionState(Context c1, Context c2) {
        Context c = now(c1, c2);
        if(c != null) {
            return  c.getStatus() == State.TRACTION;
        }
        return false;
    }

    private static boolean inBrakeState(Context c1, Context c2) {
        Context c = now(c1, c2);
        if(c != null) {
            return  c.getStatus() == State.BRAKE;
        }
        return false;
    }


    public static boolean bfun(String name, Context context1, Context context2) {
        boolean value = false;
        switch (name) {
            case "electric_range":
                value = BFuncHelper.electricRange(context1);
                break;
            case "voltage_range":
                value = BFuncHelper.voltageRange(context1);
                break;
            case "acc_range":
                value = BFuncHelper.accRange(context1);
                break;
            case "acc_rate_range":
                value = BFuncHelper.accRateRange(context1, context2);
                break;
            case "max_speed":
                value = BFuncHelper.maxSpeed(context1, context2);
                break;
            case "min_speed":
                value = BFuncHelper.minSpeed(context1, context2);
                break;

            case "trans_to_brake":
                value = BFuncHelper.transToBrake(context1, context2);
                break;

            case "trans_to_traction":
                value = BFuncHelper.transToTraction(context1, context2);
                break;
            case "in_traction_state":
                value = BFuncHelper.inTractionState(context1, context2);
                break;
            case "in_brake_state":
                value = BFuncHelper.inBrakeState(context1, context2);
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
