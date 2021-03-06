package com.mongodb.flightxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Days;

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

	private long delay = 50;
	private long increment = 5;
	private String region;

	@Override
	public void startGenerator(String region) {
		this.region = region;
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
			String resourceName = null;
			if (region != null) {
				resourceName = "classpath:data/faTracks_" + region + ".json";
			} else {
				resourceName = "classpath:data/faTracks.json";
			}
			InputStream is = resourceLoader.getResource(resourceName).getInputStream();
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

				TrackStruct lastTrackStruct = null;
				int secondOfDay = 0;
				TrackStruct firstEvent = trackData.get(0);
				TrackStruct lastEvent = trackData.get(trackData.size()-1);
				DateTime startDate = new DateTime(firstEvent.getTimestamp()*1000L);
				DateTime endDate = new DateTime(lastEvent.getTimestamp()*1000L);
				
				for (TrackStruct trackStruct : trackData) {
					trackStruct.setParent(track);
					
					
					DateTime currentPointDate = new DateTime(trackStruct.getTimestamp()*1000L);
					secondOfDay = currentPointDate.getSecondOfDay();
					
					// dirty hack
					if (currentPointDate.getDayOfYear() > startDate.getDayOfYear()) {
						secondOfDay = currentPointDate.getSecondOfDay() + startDate.getSecondOfDay();
					}
					//logger.debug(secondOfDay+"");
                    
					//secondOfDay = trackStruct.getTimestamp() - firstTimestamp;
					List<TrackStruct> list = trackBuckets.get(secondOfDay);
					if (list == null) {
						list = new ArrayList<TrackStruct>();
						trackBuckets.put(secondOfDay, list);
					}
					list.add(trackStruct);

					lastTrackStruct = trackStruct;
				}

				// add a landing event by setting groundspeed to 0 (may not be
				// at the airport thought!)
				lastTrackStruct = (TrackStruct) lastTrackStruct.clone();
				lastTrackStruct.setGroundspeed(0);
				List<TrackStruct> list = trackBuckets.get(++secondOfDay);
				if (list == null) {
					list = new ArrayList<TrackStruct>();
					trackBuckets.put(secondOfDay, list);
				}
				list.add(lastTrackStruct);

				lineCount++;
			}
			logger.debug("Read " + lineCount + " from " + resourceName);

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

		int second = 0;

		// Iterator<List<TrackStruct>> minuteBucketsIterator =
		// trackBuckets.values().iterator();
		try {
			// while (minuteBucketsIterator.hasNext() && requestStop == false) {
			while (requestStop == false) {

				// List<TrackStruct> list = minuteBucketsIterator.next();
				List<TrackStruct> list = trackBuckets.get(second++);
				if (list != null) {
					// logger.debug(list.size()+"");
					for (TrackStruct track : list) {
						// logger.debug(new
						// DateTime(track.getTimestamp()*1000L)+" "+track.getTimestamp());
						DBObject geoTrack = convertTrackStructToGeoTrack(track);
						flightTrack.insert(geoTrack);
					}
				}
				Thread.sleep(delay);
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

	@Override
	public void increaseDelay() {
		delay += increment;
		logger.debug("delay " + delay);
	}

	@Override
	public void decreaseDelay() {
		if (delay - increment > 0) {
			delay -= increment;
			logger.debug("delay " + delay);
		}

	}

}
