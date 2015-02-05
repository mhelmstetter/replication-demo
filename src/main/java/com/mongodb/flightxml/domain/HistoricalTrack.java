package com.mongodb.flightxml.domain;

import java.util.List;

/**
 * This class is used by the scraper
 *
 */
public class HistoricalTrack {
    
    
    private List<TrackStruct> data;

    public List<TrackStruct> getData() {
        return data;
    }

    public void setData(List<TrackStruct> data) {
        this.data = data;
    }

}
