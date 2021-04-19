package cn.edu.nju.context;

import java.util.Objects;

/**
 * Created by njucjc on 2017/10/3.
 */
public class Context {
    private int no;
    private String type;
    private String id;
    private String typeName;
    private int group;
    private double longitude;
    private double latitude;
    private double altitude;
    private double speed;
    private double course;
    private long timestamp;

    public Context(int no, String type, String id, String typeName,
                   int group, double longitude, double latitude, double altitude,
                   double speed, double course, long timestamp) {
        this.no = no;
        this.type = type;
        this.id = id;
        this.typeName = typeName;
        this.group = group;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.speed = speed;
        this.course = course;
        this.timestamp = timestamp;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getCourse() {
        return course;
    }

    public void setCourse(double course) {
        this.course = course;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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


    public String allForString() {
        return no + "," + type + "," + id + ","
                + typeName + "," + group + "," +
                longitude + "," + latitude + "," +
                altitude + "," + speed + "," + course + "," + timestamp;
    }

    @Override
    public String toString() {
        return ContextParser.contextToJsonWithNo(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context context = (Context) o;

        return Objects.equals(id, context.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
