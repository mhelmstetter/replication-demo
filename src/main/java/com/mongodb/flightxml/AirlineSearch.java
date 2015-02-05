package com.mongodb.flightxml;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class AirlineSearch {
    
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(Flight24Scraper.class);
    
    private Resource airlinesDatResource;
    
    private Map<String, String> icaoCodesMap = new HashMap<String, String>();
    
    @PostConstruct
    public void init() throws IOException {
        Reader in = new FileReader(airlinesDatResource.getFile());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
        for (CSVRecord record : records) {
            //logger.debug(record.toString());
            String airline = record.get(1);
            String code = record.get(4);
            //logger.debug(airline + " " + code);
            icaoCodesMap.put(airline, code);
        }
    }
    
    public String findIcaoCode(String airline) {
        
        String code = icaoCodesMap.get(airline);
        if (code == null) {
            logger.warn("No code for " + airline);
        }
        return code;
    }

    public void setAirlinesDatResource(Resource airlinesDatResource) {
        this.airlinesDatResource = airlinesDatResource;
    }

}
