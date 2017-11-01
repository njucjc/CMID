package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public Context parseContext(int id, String pattern) {
        String [] fields = pattern.split(",");
        String timestamp = fields[0];
        String plateNumber = fields[2];
        double longitude = Double.parseDouble(fields[3]);
        double latitude = Double.parseDouble(fields[4]);
        double speed = Double.parseDouble(fields[5]);
        int status = Integer.parseInt(fields[7]);

        return new Context(id, timestamp, plateNumber, longitude, latitude, speed, status);
    }
}
