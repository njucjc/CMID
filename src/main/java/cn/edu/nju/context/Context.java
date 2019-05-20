package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/3.
 */
public class Context {
    private int id;
    private String timestamp;
    private String plateNumber;
    private double v;
    private double i;
    private double power;
    private double speed;
    private int status;

    public Context(int id, String timestamp, String plateNumber, double v, double i, double power, double speed, int status) {
        this.id = id;
        this.timestamp = timestamp;
        this.plateNumber = plateNumber;
        this.v = v;
        this.i = i;
        this.power = power;
        this.speed = speed;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public double getI() {
        return i;
    }

    public void setI(double i) {
        this.i = i;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String allForString() {
        return id + "," + timestamp + "," + plateNumber +"," + v + ","
                + i + "," + power + "," + speed + "," + status;
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

        return plateNumber != null ? plateNumber.equals(context.plateNumber) : context.plateNumber == null;
    }

    @Override
    public int hashCode() {
        return plateNumber != null ? plateNumber.hashCode() : 0;
    }
}
