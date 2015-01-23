package com.mongodb.flightxml.domain;

import java.util.List;


public class AirlineFlightSchedulesResult {
    
   private int nextOffset;
   
   private List<AirlineFlightSchedule> data;

    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AirlineFlightSchedulesResult: nextOffset:" + nextOffset + ", data:" + data);
        return sb.toString();
    }



    public int getNextOffset() {
        return nextOffset;
    }



    public void setNextOffset(int nextOffset) {
        this.nextOffset = nextOffset;
    }



    public List<AirlineFlightSchedule> getData() {
        return data;
    }



    public void setData(List<AirlineFlightSchedule> data) {
        this.data = data;
    }

}
