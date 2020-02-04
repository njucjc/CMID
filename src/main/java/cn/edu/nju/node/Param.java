package cn.edu.nju.node;

/**
 * Created by njucjc at 2020/2/4
 */
public class Param {
    private int pos;

    private String op;

    private String defaultValue;

    public Param(int pos, String op, String defaultValue) {
        this.pos = pos;
        this.op = op;
        this.defaultValue = defaultValue;
    }

    public int getPos() {
        return pos;
    }

    public String getOp() {
        return op;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
