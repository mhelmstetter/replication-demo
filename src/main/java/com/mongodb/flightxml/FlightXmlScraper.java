package com.mongodb.flightxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ResourceLoader;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.flightxml.domain.ArrivalFlightStruct;
import com.mongodb.flightxml.domain.FleetArrivedResult;
import com.mongodb.flightxml.domain.FlightDetails;
import com.mongodb.util.JSON;

public class FlightXmlScraper {

    protected static final Logger logger = LoggerFactory.getLogger(FlightXmlScraper.class);

    private final static String BASE_URL = "http://flightxml.flightaware.com/json/FlightXML2/";
    String username;
    String apiKey;
    String authStringEncoded;
    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private AirlineSearch airlineSearch;
    
    @Autowired
    MongoClient mongo;
    
    DBCollection tracks;

    protected List<FlightDetails> flight24FlightDetailsList;
    
    Multiset<String> flight24AirportsSet = HashMultiset.create();
    
    private List<String> airlines = Arrays.asList(new String[] {"UPS"});


    @PostConstruct
    public void init() {
        String authString = username + ":" + apiKey;
        logger.debug("authString: " + authString);
        authStringEncoded = Base64.encodeBase64String(authString.getBytes());
        DB db = mongo.getDB("flightAware");
        tracks = db.getCollection("tracks");
    }

    private String apiCall(String urlString) throws IOException {
        //System.out.println(urlString);
        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEncoded);
        InputStream is = null;
        try {
            is = urlConnection.getInputStream();
        } catch(Exception e) {
            logger.error("Connection error", e);
            throw e;
        }
        
        InputStreamReader isr = new InputStreamReader(is);

        int numCharsRead;
        char[] charArray = new char[1024];
        StringBuffer sb = new StringBuffer();
        while ((numCharsRead = isr.read(charArray)) > 0) {
            sb.append(charArray, 0, numCharsRead);
        }
        String result = sb.toString();

        //System.out.println("*** BEGIN ***");
        //System.out.println(result);
        //System.out.println("*** END ***");
        return result;
    }

    public void scrape() throws IOException, ParseException {
        //readFlight24Data();
        
        setMaximumResultSize(200);

        for (String airline : airlines) {
            FleetArrivedResult arrived = fleetArrived(airline, 1000);
            List<ArrivalFlightStruct> arrivalFlights = arrived.getArrivals();
            for (ArrivalFlightStruct arrival : arrivalFlights) {
                String arrivalJson = gson.toJson(arrival);
                DBObject arrivalDbo = (DBObject)JSON.parse(arrivalJson);
                
                int departureTime = arrival.getActualDepartureTime();
                String ident = arrival.getIdent();
                String id = ident + "-" + arrival.getOrigin() + "-" + arrival.getDestination();
                DBObject existing = tracks.findOne(new BasicDBObject("_id", id));
                if (existing != null) {
                    logger.debug(id + " already exists");
                    continue;
                }
                
                String faFlightId = this.getFlightId(ident, departureTime);
                String historicalTrack = getHistoricalTrackAsString(faFlightId);
                DBObject historicalTrackDbo = (DBObject)JSON.parse(historicalTrack);
                historicalTrackDbo.put("faFlightId", faFlightId);
                historicalTrackDbo.put("airline", airline);
                
                historicalTrackDbo.put("_id", id);
                historicalTrackDbo.putAll(arrivalDbo);
                tracks.save(historicalTrackDbo);
                logger.debug("Saved " + id + " depart: " + departureTime + " arrive: "  + arrival.getActualArrivalTime());
            }
        }
    }

    /**
     * Read the Flight24 data which we use to fetch the same data from
     * FlightAware
     */
    protected void readFlight24Data() {
        BufferedReader reader = null;
        try {
            flight24FlightDetailsList = new ArrayList<FlightDetails>();
            InputStream is = resourceLoader.getResource("classpath:data/tracks.json").getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));

            String currentLine = null;
            int lineCount = 0;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.length() == 0) {
                    continue;
                }

                FlightDetails flightDetails = gson.fromJson(currentLine, FlightDetails.class);
                flightDetails.reverseTrack();
                flight24FlightDetailsList.add(flightDetails);
                if (flightDetails.getAirline() != null) {
                    String airlineCode = airlineSearch.findIcaoCode(flightDetails.getAirline());
                    if (airlineCode != null) {
                        flight24AirportsSet.add(airlineCode); 
                    }
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
        for (String airportCode : Multisets.copyHighestCountFirst(flight24AirportsSet).elementSet()) {
            System.out.println(airportCode + ": " + flight24AirportsSet.count(airportCode));
        }
    }
    
    //SetMaximumResultSize
    public String setMaximumResultSize(int max) throws IOException {
        String webPage = BASE_URL + String.format("SetMaximumResultSize?max_size=%s", max);
        return apiCall(webPage);
    }
    
    //FleetArrived
    public FleetArrivedResult fleetArrived(String fleet, int howMany) throws IOException {
        String webPage = BASE_URL + String.format("FleetArrived?fleet=%s&howMany=%s", fleet, howMany);
        String result = apiCall(webPage);
        JsonParser jp = new JsonParser();
        JsonObject resultElement = (JsonObject)jp.parse(result);
        FleetArrivedResult fleetArrivedResult = gson.fromJson(resultElement.get("FleetArrivedResult"), FleetArrivedResult.class);
        return fleetArrivedResult;
    }

    public String airlineFlightSchedules(Date startDate, Date endDate, String origin, String destination, String airline)
            throws IOException {
        String webPage = BASE_URL
                + String.format("AirlineFlightSchedules?startDate=%ts&endDate=%ts&origin=%s&destination=%s", startDate,
                        endDate, origin, destination, airline);
        return apiCall(webPage);

    }

    public String getFlightId(String ident, int departureTime) throws IOException {
        String webPage = BASE_URL + String.format("GetFlightID?ident=%s&departureTime=%s", ident, departureTime);
        String resultString = apiCall(webPage);
        JsonParser jp = new JsonParser();
        JsonObject resultElement = (JsonObject)jp.parse(resultString);
        return resultElement.get("GetFlightIDResult").getAsString();
    }

    public String getHistoricalTrackAsString(String faFlightId) throws IOException {
        // GetHistoricalTrack
        String webPage = BASE_URL + String.format("GetHistoricalTrack?faFlightID=%s", faFlightId);
        String result = apiCall(webPage);
        return result;
    }

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-flight-scraper.xml");
        FlightXmlScraper f = context.getBean(FlightXmlScraper.class);

        // SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy");
        // Date startDate = sdf.parse("12/7/2014");
        // Date endDate = sdf.parse("12/12/2014");
        //
        // String fs = f.airlineFlightSchedules(startDate, endDate, "KIAD",
        // "KSFO", "UAL");
        // JsonParser jp = new JsonParser();
        // JsonObject resultElement = (JsonObject)jp.parse(fs);
        //
        // System.out.println(resultElement.get("AirlineFlightSchedulesResult"));
        //
        //
        // AirlineFlightSchedulesResult result =
        // gson.fromJson(resultElement.get("AirlineFlightSchedulesResult"),
        // AirlineFlightSchedulesResult.class);
        // System.out.println(result);
        //
        // AirlineFlightSchedule flight = result.getData().get(0);
        // String flightId = f.getFlightId(flight.getFlightIdentifier(),
        // flight.getDepartureTime());
        // System.out.println(flightId);

        // String flightId = "UAL1144-1417755566-airline-0062";
        // f.getHistoricalTrack(flightId);

        // UA87
        // String flightId = f.getFlightId("UAL87", 1418459400);
        // System.out.println(flightId);

        // String flightId = "UAL87-1418279926-airline-0094";
        // f.getHistoricalTrack(flightId);

        f.scrape();

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
