package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;

/**
 * Created by njucjc on 2017/10/7.
 */
public class BFuncHelper {

    private static boolean isFly(Context c) {
        return c.getType().equals("FlyObject");
    }

    private static boolean isShip(Context c) {
        return c.getType().equals("ShipObject");
    }

    private static boolean inFlySpeedRange(Context c) {
        return c.getSpeed() >= 0 && c.getSpeed() <= 600;
    }

    private static boolean inShipSpeedRange(Context c) {
        return c.getSpeed() >= 0 && c.getSpeed() <= 20;
    }

    private static boolean inLatLonRange(Context c) {
        return c.getLongitude() >= 110 && c.getLongitude() <= 120 &&
                c.getLatitude() >= 16 && c.getLatitude() <= 24;
    }

    private static boolean inFlyAltRange(Context c) {
        return c.getAltitude() >= 0 && c.getAltitude() <= 20000;
    }

    private static boolean inShipAltRange(Context c) {
        return c.getAltitude() == 0;
    }

    private static boolean inCourseRange(Context c) {
        return c.getCourse() >= 0 && c.getCourse() <= 360;
    }

    private static boolean isSameFly(Context c1, Context c2) {
        return c1.getNo() < c2.getNo() && isFly(c1) && isFly(c2) && c1.getId().equals(c2.getId());
    }

    private static boolean isSameShip(Context c1, Context c2) {
        return c1.getNo() < c2.getNo() && isShip(c1) && isShip(c2) && c1.getId().equals(c2.getId());
    }

    private static boolean flyControllable(Context c1, Context c2) {
        double sec = Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) / 1000.0;
        double dist = LocationHelper.getDistance(c1.getLongitude(), c1.getLatitude(), c2.getLongitude(), c2.getLatitude());
        return sec != 0 && dist / sec <= 600;
    }

    private static boolean shipControllable(Context c1, Context c2) {
        double sec = Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) / 1000.0;
        double dist = LocationHelper.getDistance(c1.getLongitude(), c1.getLatitude(), c2.getLongitude(), c2.getLatitude());
        return sec != 0 && dist / sec <= 20;
    }

    private static boolean inFlyClimbSpeedRange(Context c1, Context c2) {
        if (c1.getAltitude() < 6000 && c2.getAltitude() < 6000) {
            double sec = Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) / 1000.0;
            double alt = Math.abs(c1.getAltitude() - c2.getAltitude());
            return sec != 0 && alt / sec <= 300;
        }
        else if (c1.getAltitude() > 6000 && c2.getAltitude() > 6000) {
            double sec = Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) / 1000.0;
            double alt = Math.abs(c1.getAltitude() - c2.getAltitude());
            return sec != 0 && alt / sec <= 200;
        }
        else {
            double high, low;
            if (c1.getAltitude() > c2.getAltitude()) {
                high = c1.getAltitude();
                low = c2.getAltitude();
            }
            else {
                high = c2.getAltitude();
                low = c1.getAltitude();
            }

            double totalSec = Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) / 1000.0;
            totalSec -= (6000 - low) / 300;

            if (totalSec < 0) {
                return false;
            }
            else {
                return 6000 + totalSec * 200 >= high;
            }
        }
    }



    public static boolean bfun(String name, Context context1, Context context2) {
        boolean value = false;
        switch (name) {
            case "isFly":
                value = isFly(context1);
                break;
            case "isShip":
                value = isShip(context1);
                break;
            case "inFlySpeedRange":
                value = inFlySpeedRange(context1);
                break;
            case "inShipSpeedRange":
                value = inShipSpeedRange(context1);
                break;
            case "inLatLonRange":
                value = inLatLonRange(context1);
                break;
            case "inFlyAltRange":
                value = inFlyAltRange(context1);
                break;
            case "inShipAltRange":
                value = inShipAltRange(context1);
                break;
            case "inCourseRange":
                value = inCourseRange(context1);
                break;
            case "isSameFly":
                value = isSameFly(context1, context2);
                break;
            case "isSameShip":
                value = isSameShip(context1, context2);
                break;
            case "flyControllable":
                value = flyControllable(context1, context2);
                break;
            case "shipControllable":
                value = shipControllable(context1, context2);
                break;
            case "inFlyClimbSpeedRange":
                value = inFlyClimbSpeedRange(context1, context2);
                break;
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
