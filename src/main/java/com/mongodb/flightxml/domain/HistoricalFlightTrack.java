package com.mongodb.flightxml.domain;

import com.google.gson.annotations.SerializedName;

/**
 * This class is used by the scraper
 *
 */
public class HistoricalFlightTrack {
    
    private String ident;
    private String aircrafttype;
    private String destination;
    private String origin;
    private String airline;
    
    @SerializedName("GetHistoricalTrackResult")
    private HistoricalTrack track;

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getAircrafttype() {
        return aircrafttype;
    }

    public void setAircrafttype(String aircrafttype) {
        this.aircrafttype = aircrafttype;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public HistoricalTrack getTrack() {
        return track;
    }

    public void setTrack(HistoricalTrack track) {
        this.track = track;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

}
