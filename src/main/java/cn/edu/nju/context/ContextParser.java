package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public Context parseContext(int id, String pattern) {
        String [] fields = pattern.split(",");
        String timestamp = fields[0];
        double u = Double.parseDouble(fields[1]);
        double i = Double.parseDouble(fields[2]);
        double p = Double.parseDouble(fields[3]);
        double v = Double.parseDouble(fields[4]);
        double a = Double.parseDouble(fields[5]);
        int status = Integer.parseInt(fields[6]);

        return new Context(id, timestamp, u, i, p, v, a, status);
    }

    public Context parseChangeContext(String [] elements) {

        return new Context(Integer.parseInt(elements[2]),
                elements[3],
                Double.parseDouble(elements[4]),
                Double.parseDouble(elements[5]),
                Double.parseDouble(elements[6]),
                Double.parseDouble(elements[7]),
                Double.parseDouble(elements[8]),
                Integer.parseInt(elements[9]));
    }
}
