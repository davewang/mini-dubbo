package net.iapploft.service;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.translog.Translog;

/**
 * Created by dave on 2018/9/19.
 */
public class People {
    private String wxNo;
    private String nickName;
    private String sex;

    private GeoPoint location;

    public String getWxNo() {
        return wxNo;
    }

    public void setWxNo(String wxNo) {
        this.wxNo = wxNo;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
//
//class Location{
//    private double latitude;
//    private double longitude;
//
//    public Location(double lat, double lon) {
//        this.latitude = lat;
//        this.longitude = lon;
//    }
//
//    public double getLatitude() {
//        return latitude;
//    }
//
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//
//    public double getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
//}
