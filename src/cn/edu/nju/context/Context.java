package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/3.
 */
public class Context {
    private String timestamp;
    private String plateNumber;
    private double longitude;
    private double latitude;
    private double speed;
    private int status;

    public Context(String timestamp, String plateNumber, double longitude, double latitude, double speed, int status) {
        this.timestamp = timestamp;
        this.plateNumber = plateNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
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

    @Override
    public String toString() {
//        return "Context{" +
//                "timestamp=" + timestamp +
//                ", plateNumber=" + plateNumber +
//                ", longitude=" + longitude +
//                ", latitude=" + latitude +
//                ", speed=" + speed +
//                ", status=" + status +
//                '}';
        return "cxt_" + plateNumber;
    }
}
