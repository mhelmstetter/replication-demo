package com.mongodb.flightdemo;

public class FlightInfo {

    private String flightNum;
    private double lat;
    private double lon;
    private String airline;
    
    public FlightInfo(String flightNum, double lat, double lon) {
        super();
        this.flightNum = flightNum;
        this.lat = lat;
        this.lon = lon;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public void setFlightNum(String flightNum) {
        this.flightNum = flightNum;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flightNum == null) ? 0 : flightNum.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlightInfo other = (FlightInfo) obj;
        if (flightNum == null) {
            if (other.flightNum != null)
                return false;
        } else if (!flightNum.equals(other.flightNum))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FlightInfo [flightNum=" + flightNum + ", lat=" + lat + ", lon=" + lon + "]";
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

}
