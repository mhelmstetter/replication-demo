package com.mongodb.flightxml.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Track {
    
    private double lat;
    private double lon;
    private int altitude;
    
    
    
    public Track(double lat, double lon, int altitude) {
        super();
        this.lat = lat;
        this.lon = lon;
        this.altitude = altitude;
    }
    
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLon() {
        return lon;
    }
    public void setLon(double lon) {
        this.lon = lon;
    }
    public int getAltitude() {
        return altitude;
    }
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(lat)
            .append(lon)
            .append(altitude)
            .toString();
    }
    

}
