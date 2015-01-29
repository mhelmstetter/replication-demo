package com.mongodb.flightxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.httpclient.HttpException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

@Component
public class Flight24Scraper {
    
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(Flight24Scraper.class);
    
    HtmlParser parser;
    
    @Autowired
    ResourceLoader resourceLoader;
    
    @Autowired
    MongoClient mongo;
    
    DBCollection tracks;
    DBCollection scheduled;
    
    private final static String fullUrl = "http://krk.fr24.com/zones/full_all.json";
    
    // http://arn.data.fr24.com/zones/fcgi/feed.js?flight=UA122&_=1418819308781
    
    @PostConstruct
    private void init() {
        parser = new HtmlParser();
        DB db = mongo.getDB("flight");
        tracks = db.getCollection("tracks");
        scheduled = db.getCollection("scheduled");
    }
    
    public void scrape() throws JsonIOException, JsonSyntaxException, IOException {
        //Resource resource = resourceLoader.getResource("classpath:data/full_all.json");
        
        JsonParser jp = new JsonParser();
        JsonObject resultElement = (JsonObject)jp.parse(new InputStreamReader(parser.get(fullUrl)));
        Set <Entry<String, JsonElement>> children = resultElement.entrySet();
        
        for (Iterator<Map.Entry<String, JsonElement>> i = children.iterator(); i.hasNext();) {
            Entry<String, JsonElement> entry = i.next();
            String flightId = entry.getKey();
            JsonElement value = entry.getValue();
            if (! (value instanceof JsonArray)) {
                continue;
            }
            JsonArray array = value.getAsJsonArray();
            
            String from = array.get(11).getAsString();
            String to = array.get(12).getAsString();
            String flight = array.get(13).getAsString();
            String tailNum = array.get(9).getAsString();
            
            int speed = array.get(5).getAsInt();
            
            
            if (flight.equals("") || from.equals("") || to.equals("")) {
                continue;
            }
            
            if (speed > 100) {
                continue;
            }
            
            String id = String.format("%s-%s-%s", flight, from, to);
            DBObject existing = tracks.findOne(new BasicDBObject("_id", id));
            if (existing != null) {
                logger.debug(flightId + " " + id + " already exists");
                continue;
            }
            
            //http://krk.fr24.com/_external/planedata_json.1.3.php?f=50838e7
            JsonObject flightDetails = getFlightDetails(flightId);
            
            
            
            JsonElement flightElement = flightDetails.get("flight");
            if (flightElement == null) {
                continue;
            }
            JsonElement statusElement = flightDetails.get("status");
            String status = "";
            if (statusElement != null) {
                status = statusElement.getAsString();
            }
            
            
            if (! flight.equals("") && status.equals("landed")) {
                logger.debug(flight + " status: " + status + " " + tailNum);
                DBObject dbo = (DBObject)JSON.parse(flightDetails.toString());
                dbo.put("_id", id);
                dbo.put("flightId", flightId);
                dbo.put("tailNum", tailNum);
                dbo.put("from", from);
                dbo.put("to", to);
                WriteResult result = tracks.save(dbo);
            } else if (status.equals("scheduled")) {
                logger.debug(flight + " status: " + status + " speed: " + speed);
                DBObject dbo = new BasicDBObject();
                dbo.put("_id", flightId);
                dbo.put("tailNum", tailNum);
                dbo.put("flight", flight);
                scheduled.save(dbo);
            }
            
        }
    }
    
    private JsonObject getFlightDetails(String flightId) throws HttpException, IOException {
        JsonParser jp = new JsonParser();
        String url = String.format("http://krk.fr24.com/_external/planedata_json.1.3.php?f=%s", flightId);
        InputStream is =  parser.get(url);
        
        if (is == null) {
            return null;
        }
        JsonObject flightDetails = (JsonObject)jp.parse(new InputStreamReader(is));
            
        
        flightDetails.remove("airline_url");
        flightDetails.remove("image");
        flightDetails.remove("imagelink");
        flightDetails.remove("copyright");
        flightDetails.remove("image_large");
        flightDetails.remove("imagelink_large");
        flightDetails.remove("copyright_large");
        
        return flightDetails;
    }
    
    public void scrapeScheduled() throws HttpException, IOException {
        logger.debug("count: " + scheduled.count());
        for (DBCursor cursor = scheduled.find(); cursor.hasNext();) {
            DBObject s = cursor.next();
            String flightId = (String)s.get("_id");
            
            DBObject existing = tracks.findOne(new BasicDBObject("q", flightId));
            if (existing != null) {
                logger.debug(flightId + " " + flightId + " already exists");
                scheduled.remove(s);
                continue;
            }
            
            JsonObject flightDetails = getFlightDetails(flightId);
            
            JsonElement flightElement = flightDetails.get("flight");
            if (flightElement == null) {
                continue;
            }
            JsonElement statusElement = flightDetails.get("status");
            String status = "";
            if (statusElement != null) {
                status = statusElement.getAsString();
            }
            String flight = flightElement.getAsString();
            JsonElement fromElement = flightDetails.get("from_iata");
            String from = null;
            if (fromElement != null) {
                from = fromElement.getAsString();
            }
            
            JsonElement toElement = flightDetails.get("to_iata");
            String to = null;
            if (toElement != null) {
                to = toElement.getAsString();
            }
            
            String id = String.format("%s-%s-%s", flight, from, to);
            
            if (! flight.equals("") && status.equals("landed")) {
                DBObject dbo = (DBObject)JSON.parse(flightDetails.toString());
                dbo.putAll(dbo);
                dbo.put("_id", id);
                logger.debug(dbo.toString());
                WriteResult result = tracks.save(dbo);
                scheduled.remove(s);
            }
        }
    }

    public static void main(String[] args) throws JsonIOException, JsonSyntaxException, IOException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-flight-scraper.xml");
        Flight24Scraper scraper = ctx.getBean(Flight24Scraper.class);
        scraper.scrape();
        //scraper.scrapeScheduled();

    }

}
