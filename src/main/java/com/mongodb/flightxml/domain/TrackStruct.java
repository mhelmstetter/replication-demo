package com.mongodb.flightxml.domain;

import org.joda.time.DateTime;

public class TrackStruct {

    private int altitude;
    private String updateType;
    private String altitudeChange;
    private String altitudeStatus;
    private int groundspeed;
    private int timestamp;
    private float latitude;
    private float longitude;
    
    private transient HistoricalFlightTrack parent;
    
    public DateTime getDateTime() {
        return new DateTime(timestamp);
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getAltitudeChange() {
        return altitudeChange;
    }

    public void setAltitudeChange(String altitudeChange) {
        this.altitudeChange = altitudeChange;
    }

    public String getAltitudeStatus() {
        return altitudeStatus;
    }

    public void setAltitudeStatus(String altitudeStatus) {
        this.altitudeStatus = altitudeStatus;
    }

    public int getGroundspeed() {
        return groundspeed;
    }

    public void setGroundspeed(int groundspeed) {
        this.groundspeed = groundspeed;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public HistoricalFlightTrack getParent() {
        return parent;
    }

    public void setParent(HistoricalFlightTrack parent) {
        this.parent = parent;
    }
}
