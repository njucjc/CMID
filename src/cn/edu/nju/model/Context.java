package cn.edu.nju.model;

/**
 * Created by njucjc on 2017/10/3.
 */
public class Context {
    private int id;
    private String timestamp;
    private  String plateNumber;
    private double longitude;
    private double latitude;
    private double speed;

    public Context(int id, String timestamp, String plateNumber, double longitude, double latitude, double speed) {
        this.id = id;
        this.timestamp = timestamp;
        this.plateNumber = plateNumber;
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
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

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null) {
            return false;
        }

        if(getClass() != obj.getClass()) {
            return false;
        }

        Context c = (Context)obj;
        return id == c.getId();
    }

    @Override
    public String toString() {
        return "Context{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", plateNumber=" + plateNumber +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", speed=" + speed +
                '}';
    }
}
