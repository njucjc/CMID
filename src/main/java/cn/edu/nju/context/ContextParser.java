package cn.edu.nju.context;

import cn.edu.nju.builder.AbstractCheckerBuilder;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public static Context parseContext(int no, String pattern) {
        String [] fields = pattern.split(",");
        if (fields.length != 10) {
            System.out.println("[INFO] 数据格式错误");
            System.exit(1);
        }

        String type = null;
        String id = null;
        String typeName = null;
        int group = 0;
        double longitude = 0.0;
        double latitude = 0.0;
        double altitude = 0.0;
        double speed = 0.0;
        double course = 0.0;
        long timestamp = 0L;

        try {
            type = fields[0];
            id = fields[1];
            typeName = fields[2];
            group = Integer.parseInt(fields[3]);
            longitude = Double.parseDouble(fields[4]);
            latitude = Double.parseDouble(fields[5]);
            altitude = Double.parseDouble(fields[6]);
            speed = Double.parseDouble(fields[7]);
            course = Double.parseDouble(fields[8]);
            timestamp = Long.parseLong(fields[9]);

        } catch (NumberFormatException e) {
            System.out.println("[INFO] 数据格式错误");
            System.exit(1);
        }

        return new Context(no, type, id, typeName, group, longitude, latitude, altitude, speed, course, timestamp);
    }

    public static Context parseChangeContext(String [] elements) {

        if (elements.length != 13) {
            System.out.println("[INFO] Change格式错误");
            System.exit(1);
        }

        Context context = null;
        try {
            context = new Context(Integer.parseInt(elements[2]),
                    elements[3],
                    elements[4],
                    elements[5],
                    Integer.parseInt(elements[6]),
                    Double.parseDouble(elements[7]),
                    Double.parseDouble(elements[8]),
                    Double.parseDouble(elements[9]),
                    Double.parseDouble(elements[10]),
                    Double.parseDouble(elements[11]),
                    Long.parseLong(elements[12]));
        } catch (NumberFormatException e) {
            System.out.println("[INFO] Change格式错误");
            System.exit(1);
        }
        return context;
    }
}
