package com.mongodb.flightxml.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.annotations.SerializedName;

public class AirlineFlightSchedule {

    private String ident;
    private String actualIdent;
    
    @SerializedName("departuretime")
    private int departureTime;
    
    @SerializedName("arrivaltime")
    private int arrivalTime;
    
    private String origin;
    private String destination;
    
    @SerializedName("aircrafttype")
    private String aircraftType;

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getActualIdent() {
        return actualIdent;
    }

    public void setActualIdent(String actualIdent) {
        this.actualIdent = actualIdent;
    }
    
    public String getFlightIdentifier() {
        //return actualIdent != null ? actualIdent : ident;
        if (actualIdent.equals("")) {
            return ident;
        }
        return actualIdent;
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(getFlightIdentifier())
            .append(aircraftType)
            .append(departureTime)
            .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getFlightIdentifier())
            .append(departureTime)
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AirlineFlightSchedule)) {
            return false;
            }
        AirlineFlightSchedule rhs = (AirlineFlightSchedule) obj;
        return new EqualsBuilder()
            .append(getFlightIdentifier(), rhs.getFlightIdentifier())
            .append(departureTime, rhs.departureTime)
            .isEquals();
    }

}
