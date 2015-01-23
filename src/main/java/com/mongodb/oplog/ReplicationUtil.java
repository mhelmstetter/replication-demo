// Copyright (C) 2010  Wordnik, Inc.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your 
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// General Public License for more details.  You should have received a copy 
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.

package com.mongodb.oplog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.replication.domain.ReplicationSourceStatus;
import com.wordnik.system.mongodb.MongoUtil;

public class ReplicationUtil extends MongoUtil {
    
    protected static final Logger logger = LoggerFactory.getLogger(ReplicationUtil.class);

    protected String sourceHost = null;
    protected static String DATABASE_USER_NAME = null;
    protected static String DATABASE_PASSWORD = null;

    protected static String DEST_DATABASE_USER_NAME = null;
    protected static String DEST_DATABASE_PASSWORD = null;
    protected String destinationDatabaseHost = null;
    protected static String DEST_DATABASE_NAME = null;

    protected static String DATABASE_MAPPING = null;

    protected static String OPLOG_LAST_FILENAME = "last_timestamp.txt";
    protected static String COLLECTIONS_STRING;

    private int destinationDatabasePort = 27017;
    private int sourceDatabasePort;
    
    OplogEventListener processor;
    
    OplogTailThread thd;

    

    public ReplicationUtil(ReplicationSourceStatus replicationStatus, String destHost, int destPort, OplogEventListener processor) {
        this.sourceHost = replicationStatus.getReplicationSource().getHostname();
        this.sourceDatabasePort = replicationStatus.getReplicationSource().getPort();
        this.destinationDatabaseHost = destHost;
        this.destinationDatabasePort = destPort;
        
        
        
        this.processor = processor;
        this.run();
    }

    

    protected void run() {
        // create and configure a replication target processor
        OplogReplayWriter oplogReplayWriter = new OplogReplayWriter();

        if (DATABASE_MAPPING != null) {
            Map<String, String> mappings = getMappings(DATABASE_MAPPING);
            for (Iterator<String> x = mappings.keySet().iterator(); x.hasNext();) {
                String key = x.next();
                String value = mappings.get(key);
                oplogReplayWriter.addDatabaseMapping(key, value);
            }
        }

        logger.debug(String.format("Configuring OplogReplayWriter destination %s:%s", destinationDatabaseHost, destinationDatabasePort));
        
        oplogReplayWriter.setDestinationDatabaseUsername(DEST_DATABASE_USER_NAME);
        oplogReplayWriter.setDestinationDatabasePassword(DEST_DATABASE_PASSWORD);
        oplogReplayWriter.setDestinationDatabaseHost(destinationDatabaseHost);
        oplogReplayWriter.setDestinationDatabasePort(destinationDatabasePort);

        try {
            // create and configure a tail thread
            
            logger.debug(String.format("Configuring OplogTailThread source %s:%s", sourceHost, sourceDatabasePort));
            MongoClient mongoClient = new MongoClient(sourceHost, sourceDatabasePort);
            DBCollection coll = mongoClient.getDB("local").getCollection("oplog.rs");

            // DBCollection coll = MongoDBConnectionManager.getOplog("oplog",
            // DATABASE_HOST, DATABASE_USER_NAME, DATABASE_PASSWORD).get();
            thd = new OplogTailThread(oplogReplayWriter, coll);
            thd.addOplogProcessor(processor);
            List<String> inclusions = new ArrayList<String>();
            List<String> exclusions = new ArrayList<String>();
            selectCollections(COLLECTIONS_STRING, inclusions, exclusions);

            thd.setInclusions(inclusions);
            thd.setExclusions(exclusions);
            thd.start();

            // StopFileMonitor mon = new StopFileMonitor(thd);
            // mon.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void requestStop() {
        thd.requestStop();
    }

    private Map<String, String> getMappings(String mappingString) {
        Map<String, String> output = new HashMap<String, String>();
        StringTokenizer tk = new StringTokenizer(mappingString, ",");

        while (tk.hasMoreTokens()) {
            String token = tk.nextToken();
            StringTokenizer tk2 = new StringTokenizer(token, ":");
            if (tk2.countTokens() == 2) {
                String src = tk2.nextToken();
                String dest = tk2.nextToken();
                System.out.println(src + ", " + dest);
                output.put(src, dest);
            }
        }

        return output;
    }

}
