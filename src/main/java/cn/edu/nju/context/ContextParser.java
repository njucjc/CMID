package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public Context parseContext(int id, String pattern) {
        String [] fields = pattern.split(",");
        String timestamp = fields[0];
        String plateNumber = fields[1];
        double longitude = Double.parseDouble(fields[2]);
        double latitude = Double.parseDouble(fields[3]);
        double speed = Double.parseDouble(fields[4]);
        int status = Integer.parseInt(fields[6]);

        return new Context(id, timestamp, plateNumber, longitude, latitude, speed, status);
    }

    public Context parseChangeContext(String [] elements) {

        return new Context(Integer.parseInt(elements[2]),
                elements[3],
                elements[4],
                Double.parseDouble(elements[5]),
                Double.parseDouble(elements[6]),
                Double.parseDouble(elements[7]),
                Integer.parseInt(elements[8]));
    }
}
