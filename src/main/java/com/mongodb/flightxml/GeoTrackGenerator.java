package com.mongodb.flightxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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

public class GeoTrackGenerator implements Runnable {


    protected static final Logger logger = LoggerFactory.getLogger(GeoTrackGenerator.class);

    @Autowired
    ResourceLoader resourceLoader;

    MongoClient mongoClient;

    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private String outputDatabaseName;
    
    private String outputCollectionName;
    
    private List<FlightDetails> flightDetailsList;

    DBCollection flightTrack;

    private Thread thread;

    private boolean requestStop = false;
    
    private int maxFlights = 100;
    
    private String region;

    @PostConstruct
    private void init() {
        DB db = mongoClient.getDB(outputDatabaseName);
        flightTrack = db.getCollection(outputCollectionName);
        // flightTrack.drop();
    }

    private void readData() {
        BufferedReader reader = null;
        try {
        flightDetailsList = new ArrayList<FlightDetails>();
        InputStream is = resourceLoader.getResource("classpath:data/tracks_west.json").getInputStream();
        reader = new BufferedReader(new InputStreamReader(is));

        String currentLine = null;
        int lineCount = 0;
        while ((currentLine = reader.readLine()) != null && lineCount < maxFlights) {
            if (currentLine.length() == 0) {
                continue;
            }

            FlightDetails flightDetails = gson.fromJson(currentLine, FlightDetails.class);
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

    @Override
    public void run() {
        logger.debug("Starting to generate");
        int iteration = 0;
        try {
            while (true) {
                if (requestStop) {
                    break;
                }
                Track track = null;
                for (Iterator<FlightDetails> i = flightDetailsList.iterator(); i.hasNext();) {
                    FlightDetails flightDetails = i.next();
                    track = flightDetails.getNextTrack();
                    if (track == null) {
                        // logger.debug("No more tracks " +
                        // flightDetails.getTrackPosition() + " " +
                        // flightDetails);
                        i.remove();
                        continue;
                    }

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

                    flightTrack.insert(geoTrack);
                }

                logger.debug("Iteration " + iteration++ + " " + flightDetailsList.size());
                if (flightDetailsList.size() == 0 || requestStop) {
                    break;
                }
                Thread.sleep(500);
            }
        } catch (Exception ie) {
            logger.warn("interrupted");
        }
    }

    public void startGenerator() {
        requestStop = false;
        if (flightDetailsList == null) {
            readData();
        }

        thread = new Thread(this, "GeoTrackGenerator");
        thread.start();
    }

    public void stopGenerator() {
        logger.debug("Stopping");
        requestStop = true;
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "spring-track-generator.xml");
        System.setProperty("file.encoding", "UTF-8");
        GeoTrackGenerator gen = applicationContext.getBean(GeoTrackGenerator.class);
        gen.startGenerator();
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public String getOutputDatabaseName() {
        return outputDatabaseName;
    }

    public void setOutputDatabaseName(String outputDatabaseName) {
        this.outputDatabaseName = outputDatabaseName;
    }

    public String getOutputCollectionName() {
        return outputCollectionName;
    }

    public void setOutputCollectionName(String outputCollectionName) {
        this.outputCollectionName = outputCollectionName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
