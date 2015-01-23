package com.mongodb.flightxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.flightxml.domain.AirlineFlightSchedule;
import com.mongodb.flightxml.domain.AirlineFlightSchedulesResult;

public class FlightXml {

    private final static String BASE_URL = "http://flightxml.flightaware.com/json/FlightXML2/";
    String name;
    String password;
    String authStringEncoded;
    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    public FlightXml(String username, String apiKey) {
        this.name = username;
        this.password = apiKey;
        String authString = name + ":" + password;
        authStringEncoded = Base64.encodeBase64String(authString.getBytes());
    }
    
    private String read(String webPage) throws IOException {
        System.out.println(webPage);
        URL url = new URL(webPage);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEncoded);
        InputStream is = urlConnection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);

        int numCharsRead;
        char[] charArray = new char[1024];
        StringBuffer sb = new StringBuffer();
        while ((numCharsRead = isr.read(charArray)) > 0) {
            sb.append(charArray, 0, numCharsRead);
        }
        String result = sb.toString();

        System.out.println("*** BEGIN ***");
        System.out.println(result);
        System.out.println("*** END ***");
        return result;
    }

    public String airlineFlightSchedules(Date startDate, Date endDate, String origin, String destination, String airline)
            throws IOException {
        String webPage = BASE_URL
                + String.format("AirlineFlightSchedules?startDate=%ts&endDate=%ts&origin=%s&destination=%s", startDate,
                        endDate, origin, destination, airline);
        return read(webPage);
        
    }

    public String getFlightId(String ident, int departureTime) throws IOException {
        String webPage = BASE_URL + String.format("GetFlightID?ident=%s&departureTime=%s", ident, departureTime);
        return read(webPage);
    }
    
    public String getHistoricalTrack(String faFlightId) throws IOException {
        //GetHistoricalTrack
        String webPage = BASE_URL + String.format("GetHistoricalTrack?faFlightID=%s", faFlightId);
        return read(webPage);
    }

    public static void main(String[] args) throws Exception {
        FlightXml f = new FlightXml("mhelmstetter", "7837d7535cc9223e5a35f54831139a31a75a94fb");
//        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy");
//        Date startDate = sdf.parse("12/7/2014");
//        Date endDate = sdf.parse("12/12/2014");
//        
//        String fs = f.airlineFlightSchedules(startDate, endDate, "KIAD", "KSFO", "UAL");
//        JsonParser jp = new JsonParser();
//        JsonObject resultElement = (JsonObject)jp.parse(fs);
//        
//        System.out.println(resultElement.get("AirlineFlightSchedulesResult"));
//        
//        
//        AirlineFlightSchedulesResult result = gson.fromJson(resultElement.get("AirlineFlightSchedulesResult"), AirlineFlightSchedulesResult.class);
//        System.out.println(result);
//
//        AirlineFlightSchedule flight = result.getData().get(0);
//        String flightId = f.getFlightId(flight.getFlightIdentifier(), flight.getDepartureTime());
//        System.out.println(flightId);
        
        //String flightId = "UAL1144-1417755566-airline-0062";
        //f.getHistoricalTrack(flightId);
        
        //UA87
        //String flightId = f.getFlightId("UAL87", 1418459400);
        //System.out.println(flightId);
        
        String flightId = "UAL87-1418279926-airline-0094";
        f.getHistoricalTrack(flightId);
        
    }

}
