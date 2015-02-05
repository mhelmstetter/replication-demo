package com.mongodb.flightxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.flightxml.domain.FlightDetails;
import com.mongodb.flightxml.domain.Track;

public class Flight24TrackReplayGenerator extends AbstractFlightTrackGenerator implements Runnable, FlightTrackGenerator {

	protected TreeMap<Integer, List<FlightDetails>> flightDetailsBuckets;
	protected List<FlightDetails> flightDetailsList;
	
    @Override
    public void run() {
        logger.debug("Starting to generate");
        
        Iterator<List<FlightDetails>> minuteBucketsIterator = flightDetailsBuckets.values().iterator();
        FlightDetails flightDetails = null;
        int iteration = 0;
        try {
            while (true) {
                if (requestStop) {
                    break;
                }
                
                if (flightDetailsList.size() < maxFlights && minuteBucketsIterator.hasNext()) {
                	List<FlightDetails> list = minuteBucketsIterator.next();
                	Iterator<FlightDetails> listIterator = list.iterator();
                	if (listIterator.hasNext()) {
                		FlightDetails next = listIterator.next();
                		flightDetailsList.add(next);
                		listIterator.remove();
                	}
                	if (flightDetails != null) {
                		logger.debug(flightDetails.getFromIata());
                	}
                }
                
                for (Iterator<FlightDetails> i = flightDetailsList.iterator(); i.hasNext();) {
                    flightDetails = i.next();
                    logger.debug(flightDetails.getFromIata());
                    Track track = flightDetails.getNextTrack();
                    if (track == null) {
                        // logger.debug("No more tracks " +
                        // flightDetails.getTrackPosition() + " " +
                        // flightDetails);
                        i.remove();
                        continue;
                    }
                    DBObject geoTrack = convertFlightDetailsToGeoTrack(flightDetails, track);
                    flightTrack.insert(geoTrack);
                }
                //DBObject dummy = new BasicDBObject();
                //flightTrack.insert(dummy);
                Thread.sleep(1000);
                logger.debug("Iteration " + iteration++ + " " + flightDetailsList.size());
                if ((flightDetailsList.size() == 0 && !minuteBucketsIterator.hasNext()) || requestStop) {
                    break;
                }
            }
        } catch (Exception ie) {
            logger.warn("interrupted", ie);
        }
        logger.debug("Finished generate");
    }
    
    protected DBObject convertFlightDetailsToGeoTrack(
            FlightDetails flightDetails, Track track) {
        DBObject geoTrack = new BasicDBObject();
        geoTrack.put("flightNum", flightDetails.getFlight());
        geoTrack.put("fromIata", flightDetails.getFromIata());
        geoTrack.put("toIata", flightDetails.getToIata());
        geoTrack.put("aircraft", flightDetails.getAircraft());
        String airline = flightDetails.getAirline();
        if (airline != null) {
            geoTrack.put("airline", airline);
        }
        if (region != null) {
            geoTrack.put("region", region);
        }
        BasicDBList latLong = new BasicDBList();
        latLong.add(track.getLat());
        latLong.add(track.getLon());
        geoTrack.put("position", latLong);
        geoTrack.put("altitude", track.getAltitude());
        return geoTrack;
    }
    
    protected void readData() {
        BufferedReader reader = null;
        try {
            flightDetailsList = new ArrayList<FlightDetails>();
            InputStream is = resourceLoader.getResource(
                    "classpath:data/tracks_east.json").getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            String currentLine = null;
            int lineCount = 0;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.length() == 0) {
                    continue;
                }

                FlightDetails flightDetails = gson.fromJson(currentLine,
                        FlightDetails.class);
                flightDetails.reverseTrack();
                flightDetailsList.add(flightDetails);

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

    
    private void bucketDataByTime() {
    	flightDetailsBuckets = new TreeMap<Integer, List<FlightDetails>>();
    	for (Iterator<FlightDetails> i = flightDetailsList.iterator(); i.hasNext();) {
            FlightDetails flightDetails = i.next();
            Date departure = flightDetails.getDepartureDateTime();
            
            int bucket = (int)flightDetails.getDeparture()/1000;
            
            List<FlightDetails> list = flightDetailsBuckets.get(bucket);
            if (list == null) {
            	list = new ArrayList<FlightDetails>();
            	flightDetailsBuckets.put(bucket, list);
            }
            list.add(flightDetails);
    	}
    	flightDetailsList.clear();
    }
    
    void bucketDataByFromAirport() {
    	flightDetailsBuckets = new TreeMap<Integer, List<FlightDetails>>();
    	for (Iterator<FlightDetails> i = flightDetailsList.iterator(); i.hasNext();) {
            FlightDetails flightDetails = i.next();
           
            
            //int bucket = (flightDetails.getFromIata() + flightDetails.getToIata()).hashCode();
            int bucket = flightDetails.getFromIata().hashCode();
            
            List<FlightDetails> list = flightDetailsBuckets.get(bucket);
            if (list == null) {
            	list = new ArrayList<FlightDetails>();
            	flightDetailsBuckets.put(bucket, list);
            }
            list.add(flightDetails);
    	}
    	flightDetailsList.clear();
    }
    
    @Override
	public void startGenerator() {
		requestStop = false;
		if (flightDetailsList == null) {
			readData();
			// bucketDataByTime();
			bucketDataByFromAirport();
		}

		thread = new Thread(this, "GeoTrackGenerator");
		thread.start();
	}
}
