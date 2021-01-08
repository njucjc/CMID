package cn.edu.nju.context;

import java.util.Objects;

/**
 * Created by njucjc at 2020/2/3
 */
public class Context {
    private int id;

    private String type;

    private String ip;

    private String location;

    private String generalState;

    private String powerState;

    private String fanState;

    private String portState;

    private double CPUUsage;

    private double CPUTemp;

    private double memUsage;

    private double closetTemp;

    private String timestamp;

    public Context(int id, String type, String ip, String location, String generalState,
                   String powerState, String fanState, String portState, double CPUUsage,
                   double CPUTemp, double memUsage, double closetTemp, String timestamp) {
        this.id = id;
        this.type = type;
        this.ip = ip;
        this.location = location;
        this.generalState = generalState;
        this.powerState = powerState;
        this.fanState = fanState;
        this.portState = portState;
        this.CPUUsage = CPUUsage;
        this.CPUTemp = CPUTemp;
        this.memUsage = memUsage;
        this.closetTemp = closetTemp;
        this.timestamp = timestamp;
    }


    public int getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public String getIp() {
        return ip;
    }

    public String getLocation() {
        return location;
    }

    public String getGeneralState() {
        return generalState;
    }

    public String getPowerState() {
        return powerState;
    }

    public String getFanState() {
        return fanState;
    }

    public String getPortState() {
        return portState;
    }

    public double getCPUUsage() {
        return CPUUsage;
    }

    public double getCPUTemp() {
        return CPUTemp;
    }

    public double getMemUsage() {
        return memUsage;
    }

    public double getClosetTemp() {
        return closetTemp;
    }

    public String allForString() {
        return id + "," + type + "," + ip + "," +
                location + "," + generalState + "," + powerState + "," +
                fanState + "," + portState + "," + CPUUsage + "," +
                CPUTemp + "," + memUsage + "," + closetTemp + "," + timestamp;
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

        return Objects.equals(ip, context.ip) && Objects.equals(location, context.location);
    }

    @Override
    public int hashCode() {
        String code = ip + "_" + location;
        return code.hashCode();
    }
}
