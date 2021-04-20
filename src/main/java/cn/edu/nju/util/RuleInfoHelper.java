package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;

public class RuleInfoHelper {
    public static String translate(String ruleName, String link) {
        String [] jsonStrArr = link.split(" ");
        Context c1 = null, c2 = null;
        if (jsonStrArr.length == 1) {
            c1 = ContextParser.jsonToContextWithNo(jsonStrArr[0]);
        }
        else if (jsonStrArr.length == 2){
            c1 = ContextParser.jsonToContextWithNo(jsonStrArr[0]);
            c2 = ContextParser.jsonToContextWithNo(jsonStrArr[1]);
        }

        switch (ruleName) {
            case "rule_00":
                return "ID为" + c1.getId() + "的飞机超速，其飞行速度为" + c1.getSpeed() + "米/秒，正常飞行速度为600米/秒以内";
            case "rule_01":
                return "ID为" + c1.getId() + "的舰船超速，其航行速度为" + c1.getSpeed() + "米/秒，正常航行速度为20米/秒以内";
            case "rule_02":
                return "ID为" + c1.getId() + "的飞机超出经纬度范围，其经度为E" + c1.getLongitude() + "，其纬度为N" + c1.getLatitude() + ", 正常经度范围为E110~E120，正常纬度范围为N16~N24之间";
            case "rule_03":
                return "ID为" + c1.getId() + "的舰船超出经纬度范围，其经度为E" + c1.getLongitude() + "，其纬度为N" + c1.getLatitude() + ", 正常经度范围为E110~E120，正常纬度范围为N16~N24之间";
            case "rule_04":
                return "ID为" + c1.getId() + "的飞机高度异常，其飞行高度为" + c1.getAltitude() + "米，正常飞行高度为20000米以内";
            case "rule_05":
                return "ID为" + c1.getId() + "的舰船高度异常，其航行高度为" + c1.getAltitude() + "米，正常航行高度为0米";
            case "rule_06":
                String type = c1.getType().equals("FlyObject") ? "飞机" : "舰船";
                return "ID为" + c1.getId() + "的" + type + "航向异常，其航向为高度为" + c1.getCourse() + "，正常航向为0~360之间";
            case "rule_07":
                return "ID为" + c1.getId() + "的飞机运行轨迹异常，起始经纬度为(" + c1.getLongitude() + "," + c1.getLatitude() + ")，终点经纬度为("  + c2.getLongitude() + "," + c2.getLatitude() +
                        ")，时间间隔为" + Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) + "毫秒";
            case "rule_08":
                return "ID为" + c1.getId() + "的舰船运行轨迹异常，起始经纬度为(" + c1.getLongitude() + "," + c1.getLatitude() + ")，终点经纬度为("  + c2.getLongitude() + "," + c2.getLatitude() +
                        ")，时间间隔为" + Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) + "毫秒";

            case "rule_09":
                return "ID为" + c1.getId() + "的飞机高度骤变异常，起始高度为" +  c1.getAltitude() + "，终点高度为"  + c2.getAltitude() + "，时间间隔为" + Math.abs(TimestampHelper.timestampDiff(c1.getTimestamp(), c2.getTimestamp())) + "毫秒";

        }
        return "";
    }
}
