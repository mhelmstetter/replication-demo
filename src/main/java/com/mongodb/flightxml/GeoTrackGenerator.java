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
import org.springframework.stereotype.Component;

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

@Component
public class GeoTrackGenerator {
    
    protected final static ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-track-generator.xml");
    
    protected static final Logger logger = LoggerFactory.getLogger(GeoTrackGenerator.class);
    
    @Autowired
    ResourceLoader resourceLoader;
    
    @Autowired
    MongoClient mongo;
    
    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    
    private List<FlightDetails> flightDetailsList = new ArrayList<FlightDetails>();
    
    DBCollection flightTrack;
    
    @PostConstruct
    private void init() {
        DB db = mongo.getDB("track");
        flightTrack = db.getCollection("flightTrack");
        //flightTrack.drop();
        
    }
    
    private void generate() throws IOException, InterruptedException {
        
        //InputStream is = resourceLoader.getResource("classpath:data/1track.json").getInputStream();
        InputStream is = resourceLoader.getResource("classpath:data/tracks.json").getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        String currentLine = null;
        int lineCount = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.length() == 0) {
                continue;
            }
            
            FlightDetails flightDetails = gson.fromJson(currentLine, FlightDetails.class);
            flightDetailsList.add(flightDetails);
            
            lineCount++;
        }
        logger.debug("Read " + lineCount);
        reader.close();
        
        int iteration = 0;
        while (true) {
            Track track = null;
            for (Iterator<FlightDetails> i = flightDetailsList.iterator(); i.hasNext();) {
                FlightDetails flightDetails = i.next();
                track = flightDetails.getNextTrack();
                if (track == null) {
                    //logger.debug("No more tracks " + flightDetails.getTrackPosition() + " " + flightDetails);
                    i.remove();
                    continue;
                }
                
                DBObject geoTrack = new BasicDBObject();
                geoTrack.put("flightNum", flightDetails.getFlight());
                geoTrack.put("fromIata", flightDetails.getFromIata());
                geoTrack.put("toIata", flightDetails.getToIata());
                geoTrack.put("aircraft", flightDetails.getAircraft());
                BasicDBList latLong = new BasicDBList();
                latLong.add(track.getLat());
                latLong.add(track.getLon());
                geoTrack.put("position", latLong);
                geoTrack.put("altitude", track.getAltitude());
                
                flightTrack.insert(geoTrack);
                
                
            }
            if (flightDetailsList.size() == 0) {
                break;
            }
            logger.debug("Iteration " + iteration++ + " " + flightDetailsList.size());
            Thread.sleep(500);
        }
        
    }
    
    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");
        GeoTrackGenerator gen = applicationContext.getBean(GeoTrackGenerator.class);
        
        
        gen.generate();
    }
}
