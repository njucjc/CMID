package cn.edu.nju.context;

/**
 * Created by njucjc at 2020/2/3
 */
public class VContext {
    private int id;

    private int type;

    private String code;

    public VContext(int id, int type, String code) {
        this.id = id;
        this.type = type;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getCode() {
        return code;
    }
}
