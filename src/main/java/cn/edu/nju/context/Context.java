package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/3.
 */
public class Context {
    private int id;
    private String timestamp;
    private double u;
    private double i;
    private double p;
    private double v;
    private double a;
    private int status;

    public Context(int id, String timestamp, double u, double i, double p, double v, double a, int status) {
        this.id = id;
        this.timestamp = timestamp;
        this.u = u;
        this.i = i;
        this.p = p;
        this.v = v;
        this.a = a;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getU() {
        return u;
    }

    public void setU(double u) {
        this.u = u;
    }

    public double getI() {
        return i;
    }

    public void setI(double i) {
        this.i = i;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String allForString() {
        return id + "," + timestamp  +"," + u + ","
                + i + "," + p + "," + v + "," + a + "," + status;
    }

    @Override
    public String toString() {
        return "ctx_" + id;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Context context = (Context) o;
//
//        return plateNumber != null ? plateNumber.equals(context.plateNumber) : context.plateNumber == null;
//    }
//
//    @Override
//    public int hashCode() {
//        return plateNumber != null ? plateNumber.hashCode() : 0;
//    }
}
