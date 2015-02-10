package com.mongodb.flightxml;

public interface FlightTrackGenerator {

	public void startGenerator(String region);

	public void stopGenerator();

	public void setOutputDatabaseName(String outputDatabaseName);

	public void setOutputCollectionName(String outputCollectionName);

	public void setRegion(String region);
	
	public void increaseDelay();
	
	public void decreaseDelay();

}