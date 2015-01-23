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

public class FlightXmlDummy {

    private final static String BASE_URL = "http://flightxml.flightaware.com/json/FlightXML2/";
    String name;
    String password;
    String authStringEncoded;
    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public FlightXmlDummy(String username, String apiKey) {
        this.name = username;
        this.password = apiKey;
        String authString = name + ":" + password;
        authStringEncoded = Base64.encodeBase64String(authString.getBytes());
    }

    public String airlineFlightSchedules(Date startDate, Date endDate, String origin, String destination, String airline)
            throws IOException {
        return "{\"AirlineFlightSchedulesResult\":{\"next_offset\":15,\"data\":[{\"ident\":\"ANZ9563\",\"actual_ident\":\"UAL1144\",\"departuretime\":1417958100,\"arrivaltime\":1417979100,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B753\",\"meal_service\":\"\",\"seats_cabin_first\":24,\"seats_cabin_business\":0,\"seats_cabin_coach\":189},{\"ident\":\"CCA7240\",\"actual_ident\":\"UAL1144\",\"departuretime\":1417958100,\"arrivaltime\":1417979100,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B753\",\"meal_service\":\"First: Breakfast, Refreshments / Economy: Food for sale\",\"seats_cabin_first\":24,\"seats_cabin_business\":0,\"seats_cabin_coach\":189},{\"ident\":\"UAL1144\",\"actual_ident\":\"\",\"departuretime\":1417958100,\"arrivaltime\":1417979100,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B753\",\"meal_service\":\"First: Breakfast, Refreshments / Economy: Food for sale\",\"seats_cabin_first\":24,\"seats_cabin_business\":0,\"seats_cabin_coach\":189},{\"ident\":\"SAA7408\",\"actual_ident\":\"UAL1144\",\"departuretime\":1417958100,\"arrivaltime\":1417979100,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B753\",\"meal_service\":\"\",\"seats_cabin_first\":24,\"seats_cabin_business\":0,\"seats_cabin_coach\":189},{\"ident\":\"UAL217\",\"actual_ident\":\"\",\"departuretime\":1417960080,\"arrivaltime\":1417981680,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B752\",\"meal_service\":\"Business: Food for sale / Economy: Food for sale\",\"seats_cabin_first\":0,\"seats_cabin_business\":16,\"seats_cabin_coach\":153},{\"ident\":\"CCA7241\",\"actual_ident\":\"UAL217\",\"departuretime\":1417960080,\"arrivaltime\":1417981680,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B752\",\"meal_service\":\"Business: Food for sale / Economy: Food for sale\",\"seats_cabin_first\":0,\"seats_cabin_business\":16,\"seats_cabin_coach\":153},{\"ident\":\"ANZ9571\",\"actual_ident\":\"UAL217\",\"departuretime\":1417960080,\"arrivaltime\":1417981680,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B752\",\"meal_service\":\"\",\"seats_cabin_first\":0,\"seats_cabin_business\":16,\"seats_cabin_coach\":153},{\"ident\":\"VRD69\",\"actual_ident\":\"\",\"departuretime\":1417964400,\"arrivaltime\":1417985700,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"A320\",\"meal_service\":\"\",\"seats_cabin_first\":8,\"seats_cabin_business\":0,\"seats_cabin_coach\":141},{\"ident\":\"SIA1389\",\"actual_ident\":\"VRD69\",\"departuretime\":1417964400,\"arrivaltime\":1417985700,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"A320\",\"meal_service\":\"\",\"seats_cabin_first\":8,\"seats_cabin_business\":0,\"seats_cabin_coach\":141},{\"ident\":\"SQC1389\",\"actual_ident\":\"VRD69\",\"departuretime\":1417964400,\"arrivaltime\":1417985700,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"A320\",\"meal_service\":\"\",\"seats_cabin_first\":8,\"seats_cabin_business\":0,\"seats_cabin_coach\":141},{\"ident\":\"ETH1358\",\"actual_ident\":\"UAL1727\",\"departuretime\":1417972800,\"arrivaltime\":1417994460,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B739\",\"meal_service\":\"\",\"seats_cabin_first\":20,\"seats_cabin_business\":0,\"seats_cabin_coach\":147},{\"ident\":\"SAA7438\",\"actual_ident\":\"UAL1727\",\"departuretime\":1417972800,\"arrivaltime\":1417994460,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B739\",\"meal_service\":\"\",\"seats_cabin_first\":20,\"seats_cabin_business\":0,\"seats_cabin_coach\":147},{\"ident\":\"ANZ9565\",\"actual_ident\":\"UAL1727\",\"departuretime\":1417972800,\"arrivaltime\":1417994460,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B739\",\"meal_service\":\"\",\"seats_cabin_first\":20,\"seats_cabin_business\":0,\"seats_cabin_coach\":147},{\"ident\":\"UAL1727\",\"actual_ident\":\"\",\"departuretime\":1417972800,\"arrivaltime\":1417994460,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"B739\",\"meal_service\":\"First: Lunch, Refreshments / Economy: Food for sale\",\"seats_cabin_first\":20,\"seats_cabin_business\":0,\"seats_cabin_coach\":147},{\"ident\":\"ANZ9573\",\"actual_ident\":\"UAL225\",\"departuretime\":1417980780,\"arrivaltime\":1418002500,\"origin\":\"KIAD\",\"destination\":\"KSFO\",\"aircrafttype\":\"A320\",\"meal_service\":\"\",\"seats_cabin_first\":12,\"seats_cabin_business\":0,\"seats_cabin_coach\":126}]}}";
    }

    public String getFlightId(String ident, int departureTime) throws IOException {
        String webPage = BASE_URL + String.format("GetFlightID?ident=%s&departureTime=%s", ident, departureTime);
        System.out.println(webPage);

        return "xxx";
    }

    public static void main(String[] args) throws Exception {
        FlightXmlDummy f = new FlightXmlDummy("mhelmstetter", "7837d7535cc9223e5a35f54831139a31a75a94fb");
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy");
        Date startDate = sdf.parse("12/7/2014");
        Date endDate = sdf.parse("12/12/2014");

        String fs = f.airlineFlightSchedules(startDate, endDate, "KIAD", "KSFO", "UAL");
        JsonParser jp = new JsonParser();
        JsonObject resultElement = (JsonObject) jp.parse(fs);

        System.out.println(resultElement.get("AirlineFlightSchedulesResult"));

        AirlineFlightSchedulesResult result = gson.fromJson(resultElement.get("AirlineFlightSchedulesResult"),
                AirlineFlightSchedulesResult.class);

        for (AirlineFlightSchedule flight : result.getData()) {
            System.out.println(flight);
        }

    }

}
