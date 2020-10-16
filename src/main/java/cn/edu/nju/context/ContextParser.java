package cn.edu.nju.context;

import cn.edu.nju.builder.AbstractCheckerBuilder;

/**
 * Created by njucjc on 2017/10/23.
 */
public class ContextParser {
    public Context parseContext(int id, String pattern) {
        String [] fields = pattern.split(",");
        String timestamp = id + "";
        if (fields.length < 2) {
            System.out.println("[INFO] '"+ AbstractCheckerBuilder.dataFilePath + "'文件格式错误");
            System.exit(1);
        }

        String code = fields[0];
        int type = Integer.parseInt(fields[1]);

        return new Context(id,timestamp, code, type);
    }

    public Context parseChangeContext(String [] elements) {

        if (elements.length < 6) {
            System.out.println("[INFO] '"+ AbstractCheckerBuilder.changeFilePath + "'文件格式错误");
            System.exit(1);
        }
        Context context = null;
        try {
            context = new Context(Integer.parseInt(elements[2]),
                    elements[3],
                    elements[4],
                    Integer.parseInt(elements[5]));
        } catch (NumberFormatException e) {
            System.out.println("[INFO] '"+ AbstractCheckerBuilder.changeFilePath + "'文件格式错误");
            System.exit(1);
        }

        return context;
    }
}
