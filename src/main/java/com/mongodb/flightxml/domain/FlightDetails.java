package com.mongodb.flightxml.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.annotations.SerializedName;

public class FlightDetails {
    
    private String _id;
    private String flight;
    private String fromIata;
    private String toIata;
    private String airline;
    private String aircraft;
    private long departure;
    private long dep_schd;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(flight)
            .append(fromIata)
            .append(toIata)
            .append(airline)
            .append(aircraft)
            //.append(track)
            .toString();
    }
    
    @SerializedName("trail")
    private double[] track;
    
    private int trackPosition = -1;
    
    public void reverseTrack() {
        Collections.reverse(Arrays.asList(track));
    }

    public Track getNextTrack() {
        if (trackPosition+3 <= track.length) {
            return new Track(track[trackPosition++], track[trackPosition++], (int)track[trackPosition++]);
        }
        return null;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flightNum) {
        this.flight = flightNum;
    }

    public String getFromIata() {
        return fromIata;
    }

    public void setFromIata(String fromIata) {
        this.fromIata = fromIata;
    }

    public String getToIata() {
        return toIata;
    }

    public void setToIata(String toIata) {
        this.toIata = toIata;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getAircraft() {
        return aircraft;
    }

    public void setAircraft(String aircraft) {
        this.aircraft = aircraft;
    }

    public double[] getTrack() {
        return track;
    }

    public void setTrack(double[] track) {
        this.track = track;
    }

    public int getTrackPosition() {
        return trackPosition;
    }

    public void setDeparture(long departure) {
	this.departure = departure;
    }

    public long getDeparture() {
	return this.departure;
    }

    public Date getDepartureDateTime() {
	return new Date(departure);
    }

    public long getDep_schd() {
        return dep_schd;
    }
}
