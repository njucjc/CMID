package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.Param;

import java.util.List;

/**
 * Created by njucjc at 2020/2/3
 */
public class BFuncHelper {

    private static String ONLINE = "在线";
    private static String NORMAL = "正常";

    private static boolean isOnline(Context c) {
        return c.getGeneralState().equals(ONLINE);
    }

    private static boolean areThreeStateNormal(Context c) {
        return c.getPowerState().equals(NORMAL) && c.getFanState().equals(NORMAL) && c.getPortState().equals(NORMAL);
    }

    private static boolean isOverload(Context c) {
        return c.getCPUTemp() > 50.0 ||
                c.getCPUUsage() > 60.0 ||
                c.getMemUsage() > 60.0 ||
                c.getClosetTemp() > 45.0;
    }

    private static boolean isLowState(Context c) {
        return c.getCPUTemp() < 0.0 || c.getClosetTemp() < 5.0;
    }

    private static boolean isSameIP(List<Context> list) {
        if (list.size() == 0) {
            return false;
        }
        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getId() >= c2.getId() || !c1.getIp().equals(c2.getIp()) || !c1.getLocation().equals(c2.getLocation())) return false;
        }
        return true;
    }

    private static boolean isOneHourInterval(Context c1, Context c2) {
        return c1.getId() < c2.getId() && TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp()) == 3600000;
    }

    private static boolean isHalfHourInterval(Context c1, Context c2) {
        return c1.getId() < c2.getId() && TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp()) == 1800000;
    }

    private static boolean duringOneHourInterval(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }

        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getId() >= c2.getId()) return false;
        }
        return TimestampHelper.timestampDiff(list.get(0).getTimestamp(), list.get(list.size() - 1).getTimestamp()) <= 3600000;
    }

    private static boolean duringHalfHourInterval(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }

        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getId() >= c2.getId()) return false;
        }
        return TimestampHelper.timestampDiff(list.get(0).getTimestamp(), list.get(list.size() - 1).getTimestamp()) <= 1800000;
    }

    private static boolean isCPUTemperatureChangeAbsoluteOver(Context c1, Context c2, double a) {
        return Math.abs(c1.getCPUTemp() - c2.getCPUTemp()) > a;
    }

    private static boolean isCPUUsageChangeRelativeOver(Context c1, Context c2, double a) {
        return Math.abs(c1.getCPUUsage() - c2.getCPUUsage()) > a;
    }

    private static boolean isCPUClosetTemperatureChangeDvalueOver(Context c1, Context c2, double a) {
        return Math.abs(Math.abs(c1.getClosetTemp() - c2.getClosetTemp()) -
                Math.abs(c1.getCPUTemp() - c2.getCPUTemp()) ) > a;
    }

    private static boolean isCPUTemperatureIncrease(Context c1, Context c2) {
        //c1.id < c2.id
        return c1.getCPUTemp() < c2.getCPUTemp();
    }

    private static boolean isClosetTemperatureIncrease(Context c1, Context c2) {
        return c1.getClosetTemp() < c2.getClosetTemp();
    }

    private static boolean isPredictedCPUTemperatureOverload(List<Context> list) {
        for(Context c : list) {
            if (c.getCPUTemp() > 50.0) return true;
        }
        return false;
    }

    private static boolean isPredictedClosetTemperatureOverload(List<Context> list) {
        for (Context c : list) {
            if (c.getClosetTemp() > 45.0) return true;
        }
        return false;
    }

    private static boolean isPredictedCPUTemperatureLowState(List<Context> list) {
        for(Context c : list) {
            if (c.getCPUTemp() < 0.0) return true;
        }
        return false;
    }

    private static boolean isPredictedClosetTemperatureLowState(List<Context> list) {
        for (Context c : list) {
            if (c.getClosetTemp() < 5.0) return true;
        }
        return false;
    }

    private static boolean isCPUTemperatureConsistentlyIncrease(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }
        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getCPUTemp() > c2.getCPUTemp()) return false;
        }
        return true;
    }

    private static boolean isCPUTemperatureConsistentlyDecrease(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }
        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getCPUTemp() < c2.getCPUTemp()) return false;
        }
        return true;
    }

    private static boolean isClosetTemperatureConsistentlyIncrease(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }
        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getClosetTemp() > c2.getClosetTemp()) return false;
        }
        return true;
    }

    private static boolean isClosetTemperatureConsistentlyDecrease(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }
        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getClosetTemp() < c2.getClosetTemp()) return false;
        }
        return true;
    }

    private static boolean allMemoryUsageRapidlyChanged(List<Context> list, double a) {
        if (list.size() < 2) {
            return false;
        }
        for (int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (Math.abs(c1.getMemUsage() - c2.getMemUsage()) <= a) return false;
        }
        return true;
    }

    private static boolean isMemUsageConsistentlyIncrease(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }
        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getMemUsage() > c2.getMemUsage()) return false;
        }
        return true;
    }

    private static boolean isMemUsageConsistentlyDecrease(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }
        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getMemUsage() < c2.getMemUsage()) return false;
        }
        return true;
    }

    private static boolean allSameChangeDirection(List<Context> list) {
        return !isMemUsageConsistentlyDecrease(list) || !isMemUsageConsistentlyIncrease(list);
    }

    private static boolean isContinous(List<Context> list) {
        if (list.size() < 2) {
            return false;
        }

        for(int i = 0; i < list.size() - 1; ++i) {
            Context c1 = list.get(i);
            Context c2 = list.get(i + 1);
            if (c1.getOrder() + 1 != c2.getOrder()) return false;
        }

        return true;
    }

    public static boolean bfunc(String name, List<Param> list1, List<Context> list2) {
        boolean value = false;
        switch (name) {
            case "isOnline":
                value = isOnline(list2.get(0));
                break;
            case "areThreeStateNormal":
                value = areThreeStateNormal(list2.get(0));
                break;
            case "isOverload":
                value = isOverload(list2.get(0));
                break;
            case "isLowState":
                value = isLowState(list2.get(0));
                break;
            case "isSameIP":
                value = isSameIP(list2);
                break;
            case "isOneHourInterval":
                value = isOneHourInterval(list2.get(0), list2.get(1));
                break;
            case "isHalfHourInterval":
                value = isHalfHourInterval(list2.get(0), list2.get(1));
                break;
            case "duringOneHourInterval":
                value = duringOneHourInterval(list2);
                break;
            case "duringHalfHourInterval":
                value = duringHalfHourInterval(list2);
                break;
            case "isCPUTemperatureChangeAbsoluteOver":
                value = isCPUTemperatureChangeAbsoluteOver(list2.get(0), list2.get(1), Double.parseDouble(list1.get(list1.size() - 1).getDefaultValue()));
                break;
            case "isCPUUsageChangeRelativeOver":
                value = isCPUUsageChangeRelativeOver(list2.get(0), list2.get(1), Double.parseDouble(list1.get(list1.size() - 1).getDefaultValue()));
                break;
            case "isCPUClosetTemperatureChangeDvalueOver":
                value = isCPUClosetTemperatureChangeDvalueOver(list2.get(0), list2.get(1), Double.parseDouble(list1.get(list1.size() - 1).getDefaultValue()));
                break;
            case "isCPUTemperatureIncrease":
                value = isCPUTemperatureIncrease(list2.get(0), list2.get(1));
                break;
            case "isClosetTemperatureIncrease":
                value = isClosetTemperatureIncrease(list2.get(0), list2.get(1));
                break;
            case "isPredictedCPUTemperatureOverload":
                value = isPredictedCPUTemperatureOverload(list2);
                break;
            case "isPredictedClosetTemperatureOverload":
                value = isPredictedClosetTemperatureOverload(list2);
                break;
            case "isPredictedCPUTemperatureLowState":
                value = isPredictedCPUTemperatureLowState(list2);
                break;
            case "isPredictedClosetTemperatureLowState":
                value = isPredictedClosetTemperatureLowState(list2);
                break;
            case "isCPUTemperatureConsistentlyIncrease":
                value = isCPUTemperatureConsistentlyIncrease(list2);
                break;
            case "isCPUTemperatureConsistentlyDecrease":
                value = isCPUTemperatureConsistentlyDecrease(list2);
                break;
            case "isClosetTemperatureConsistentlyIncrease":
                value = isClosetTemperatureConsistentlyIncrease(list2);
                break;
            case "isClosetTemperatureConsistentlyDecrease":
                value = isClosetTemperatureConsistentlyDecrease(list2);
                break;
            case "allMemoryUsageRapidlyChanged":
                value = allMemoryUsageRapidlyChanged(list2, Double.parseDouble(list1.get(list1.size() - 1).getDefaultValue()));
                break;
            case "allSameChangeDirection":
                value = allSameChangeDirection(list2);
                break;
            case "isContinous":
                value = isContinous(list2);
                break;
            default:
                assert  false:"[DEBUG] Illegal bfunc: " + name;
                break;
        }
        return value;
    }
}
