package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public synchronized Context parseContext(int id, String pattern) {
        String [] fields = pattern.split(",");
        String timestamp = fields[0];
        String plateNumber = fields[1];
        double v = Double.parseDouble(fields[2]);
        double i = Double.parseDouble(fields[3]);
        double power = Double.parseDouble(fields[4]);
        double speed = Double.parseDouble(fields[5]);
        int status = Integer.parseInt(fields[6]);

        return new Context(id, timestamp, plateNumber, v, i, power, speed, status);
    }

    public synchronized Context parseChangeContext(String [] elements) {

        return new Context(Integer.parseInt(elements[2]),
                elements[3],
                elements[4],
                Double.parseDouble(elements[5]),
                Double.parseDouble(elements[6]),
                Double.parseDouble(elements[7]),
                Double.parseDouble(elements[8]),
                Integer.parseInt(elements[9]));
    }
}
