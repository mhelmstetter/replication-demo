package com.mongodb.flightxml.domain;

import com.google.gson.annotations.SerializedName;

public class ArrivalFlightStruct {
    
    @SerializedName("actualdeparturetime")
    private int actualDepartureTime;
    
    @SerializedName("actualarrivaltime")
    private int actualArrivalTime;
    
    private String ident;
    
    @SerializedName("aircrafttype")
    private String aircraftType;
    
    private String destination;
    private String destinationCity;
    private String destinationName;
    private String origin;
    private String originCity;
    private String originName;

    public int getActualDepartureTime() {
        return actualDepartureTime;
    }

    public void setActualDepartureTime(int actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public int getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(int actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginCity() {
        return originCity;
    }

    public void setOriginCity(String originCity) {
        this.originCity = originCity;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }


}
