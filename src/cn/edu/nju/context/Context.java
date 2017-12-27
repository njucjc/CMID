package cn.edu.nju.context;

/**
 * Created by njucjc on 2017/10/3.
 */
public class Context {
    private int id;
    private String timestamp;
    private String plateNumber;
    private double longitude;
    private double latitude;
    private double speed;
    private int status;

    public Context(int id, String timestamp, String plateNumber, double longitude, double latitude, double speed, int status) {
        this.id = id;
        this.timestamp = timestamp;
        this.plateNumber = plateNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
        this.status = status;
    }

    public synchronized int getId() {
        return id;
    }

    public synchronized void setId(int id) {
        this.id = id;
    }

    public synchronized String getTimestamp() {
        return timestamp;
    }

    public synchronized void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public synchronized String getPlateNumber() {
        return plateNumber;
    }

    public synchronized void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public synchronized double getLongitude() {
        return longitude;
    }

    public synchronized void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public synchronized double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public synchronized double getSpeed() {
        return speed;
    }

    public synchronized void setSpeed(double speed) {
        this.speed = speed;
    }

    public synchronized int getStatus() {
        return status;
    }

    public synchronized void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "cxt_" + id;
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
