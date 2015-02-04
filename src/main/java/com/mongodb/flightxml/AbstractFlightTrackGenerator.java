package com.mongodb.flightxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.flightxml.domain.FlightDetails;
import com.mongodb.flightxml.domain.Track;

public abstract class AbstractFlightTrackGenerator implements FlightTrackGenerator {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractFlightTrackGenerator.class);
	
	@Autowired
	ResourceLoader resourceLoader;
	MongoClient mongoClient;
	private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	private String outputDatabaseName;
	private String outputCollectionName;
	protected List<FlightDetails> flightDetailsList;
	
	protected DBCollection flightTrack;
	protected Thread thread;
	protected boolean requestStop = false;
	protected int maxFlights = 100;
	private String region;

	public AbstractFlightTrackGenerator() {
		super();
	}

	@PostConstruct
	private void init() {
		DB db = mongoClient.getDB(outputDatabaseName);
		flightTrack = db.getCollection(outputCollectionName);
		// flightTrack.drop();
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

	@Override
	public void stopGenerator() {
		logger.debug("Stopping");
		requestStop = true;
	}

	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public String getOutputDatabaseName() {
		return outputDatabaseName;
	}

	@Override
	public void setOutputDatabaseName(String outputDatabaseName) {
		this.outputDatabaseName = outputDatabaseName;
	}

	public String getOutputCollectionName() {
		return outputCollectionName;
	}

	@Override
	public void setOutputCollectionName(String outputCollectionName) {
		this.outputCollectionName = outputCollectionName;
	}

	public String getRegion() {
		return region;
	}

	@Override
	public void setRegion(String region) {
		this.region = region;
	}

}