package cn.edu.nju.context;

/**
 * Created by njucjc at 2020/2/3
 */
public class Context {
    private int id;

    private int type;

    private String code;

    private String timestamp;

    public Context(int id, String timestamp, String code, int type) {
        this.id = id;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String allForString() {
        return id + "," + timestamp + "," + code + "," + type;
    }

    @Override
    public String toString() {
        return "ctx_" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context context = (Context) o;

        return code != null ? code.equals(context.code) : context.code == null;
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}
