package cn.edu.nju.util;

import jdk.nashorn.internal.runtime.GlobalConstants;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;

/**
 * Created by njucjc on 2017/10/7.
 */
public class LocationHelper {


    public static double getDistance(double long1, double lat1, double long2, double lat2) {
        GlobalCoordinates source = new GlobalCoordinates(lat1, long1);
        GlobalCoordinates target = new GlobalCoordinates(lat2, long2);

        return new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
    }

    public static void main(String[] args) {
        //根据两点间的经纬度计算距离，单位：
    }
}
