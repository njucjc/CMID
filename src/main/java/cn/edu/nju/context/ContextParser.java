package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public Context parseContext(int id, String pattern) {
        String [] fields = pattern.split(",");
        String timestamp = id + "";
        String code = fields[0];
        int type = Integer.parseInt(fields[1]);

        return new Context(id,timestamp, code, type);
    }

    public Context parseChangeContext(String [] elements) {

        return new Context(Integer.parseInt(elements[2]),
                elements[3],
                elements[4],
                Integer.parseInt(elements[5]));
    }
}
