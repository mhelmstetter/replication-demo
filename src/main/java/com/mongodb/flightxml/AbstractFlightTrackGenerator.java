package com.mongodb.flightxml;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public abstract class AbstractFlightTrackGenerator implements FlightTrackGenerator {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractFlightTrackGenerator.class);
	
	@Autowired
	ResourceLoader resourceLoader;
	MongoClient mongoClient;
	protected static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	private String outputDatabaseName;
	private String outputCollectionName;
	
	
	protected DBCollection flightTrack;
	protected Thread thread;
	protected boolean requestStop = false;
	protected int maxFlights = 100;
	protected String region;

	public AbstractFlightTrackGenerator() {
		super();
	}

	@PostConstruct
	private void init() {
		DB db = mongoClient.getDB(outputDatabaseName);
		flightTrack = db.getCollection(outputCollectionName);
		// flightTrack.drop();
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