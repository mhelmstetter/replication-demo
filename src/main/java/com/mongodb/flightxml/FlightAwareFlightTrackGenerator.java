package com.mongodb.flightxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.flightxml.domain.HistoricalFlightTrack;
import com.mongodb.flightxml.domain.HistoricalTrack;
import com.mongodb.flightxml.domain.TrackStruct;

public class FlightAwareFlightTrackGenerator extends AbstractFlightTrackGenerator implements Runnable,
        FlightTrackGenerator {

    private List<HistoricalTrack> trackList;

    protected TreeMap<Integer, List<TrackStruct>> trackBuckets = new TreeMap<Integer, List<TrackStruct>>();

    @Override
    public void startGenerator() {
        requestStop = false;
        if (trackList == null) {
            readData();
        }

        thread = new Thread(this, "FlightAwareFlightTrackGenerator");
        thread.start();
    }

    protected void readData() {
        BufferedReader reader = null;
        try {
            trackList = new ArrayList<HistoricalTrack>();
            InputStream is = resourceLoader.getResource("classpath:data/faTracks.json").getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            String currentLine = null;
            int lineCount = 0;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.length() == 0) {
                    continue;
                }

                HistoricalFlightTrack track = gson.fromJson(currentLine, HistoricalFlightTrack.class);
                if (track.getTrack() == null) {
                    continue;
                }
                List<TrackStruct> trackData = track.getTrack().getData();

                for (TrackStruct trackStruct : trackData) {
                    trackStruct.setParent(track);
                    int ts = trackStruct.getTimestamp();
                    DateTime dt = new DateTime(ts * 1000);
                    int secondOfDay = dt.getSecondOfDay();
                    List<TrackStruct> list = trackBuckets.get(secondOfDay);
                    if (list == null) {
                        list = new ArrayList<TrackStruct>();
                        trackBuckets.put(secondOfDay, list);
                    }
                    list.add(trackStruct);
                }

                lineCount++;
            }
            logger.debug("Read " + lineCount);

        } catch (IOException ioe) {
            logger.error("Error reading data", ioe);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void run() {
        logger.debug("Starting to generate");

        Iterator<List<TrackStruct>> minuteBucketsIterator = trackBuckets.values().iterator();
        try {
            while (minuteBucketsIterator.hasNext() && requestStop == false) {

                List<TrackStruct> list = minuteBucketsIterator.next();

                for (TrackStruct track : list) {
                    DBObject geoTrack = convertTrackStructToGeoTrack(track);
                    flightTrack.insert(geoTrack);
                }
                Thread.sleep(50);
            }
            

        } catch (Exception ie) {
            logger.warn("interrupted", ie);
        }
        logger.debug("Finished generate");
    }

    private DBObject convertTrackStructToGeoTrack(TrackStruct track) {
        DBObject geoTrack = new BasicDBObject();
        geoTrack.put("flightNum", track.getParent().getIdent());
        geoTrack.put("fromIata", track.getParent().getOrigin());
        geoTrack.put("toIata", track.getParent().getDestination());
        geoTrack.put("aircraft", track.getParent().getAircrafttype());
        // TODO fixme!
        String airline = "UAL";
        if (airline != null) {
            geoTrack.put("airline", airline);
        }
        if (region != null) {
            geoTrack.put("region", region);
        }
        BasicDBList latLong = new BasicDBList();
        latLong.add(track.getLatitude());
        latLong.add(track.getLongitude());
        geoTrack.put("position", latLong);
        geoTrack.put("altitude", track.getAltitude());
        geoTrack.put("groundSpeed", track.getGroundspeed());
        return geoTrack;
    }

}