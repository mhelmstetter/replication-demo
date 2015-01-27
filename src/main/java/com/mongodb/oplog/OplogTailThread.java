// Copyright (C) 2012  Wordnik, Inc.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.BSONTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.replication.domain.ReplicationSource;
import com.mongodb.util.JSON;
import com.wordnik.util.PrintFormat;

public class OplogTailThread extends Thread {
    
    protected static final Logger logger = LoggerFactory.getLogger(OplogTailThread.class);
    
    protected boolean enableOutput = true;
    protected boolean running = false;
    protected boolean killMe = false;
    protected long reportInterval = 1000;
    protected List<String> inclusions = new ArrayList<String>();
    protected List<String> exclusions = new ArrayList<String>();
    protected List<OplogEventListener> processors = new ArrayList<OplogEventListener>();
    protected DBCollection oplog;
    protected String OPLOG_LAST_FILENAME = "last_timestamp.txt";
    
    BSONTimestamp lastTimestamp = null;
    
    private String baseQueryJson;
    private DBObject baseQuery;
    
    private MongoClient mongoClient;
    
    public OplogTailThread(ReplicationSource replicationSource) throws UnknownHostException {
        MongoClient mongoClient = new MongoClient(replicationSource.getHostname(), replicationSource.getPort());
        this.setBaseQueryJson(replicationSource.getOplogBaseQuery());
        oplog = mongoClient.getDB("local").getCollection("oplog.rs");
    }

    public void setOutputEnabled(boolean enabled) {
        this.enableOutput = enabled;
    }

    public void setStopFilename(String filename) {
        this.OPLOG_LAST_FILENAME = filename;
    }

    public void addOplogProcessor(OplogEventListener processor) {
        this.processors.add(processor);
    }

    public void setBaseDir(String dir) {
        if (dir != null) {
            OPLOG_LAST_FILENAME = dir + File.separator + OPLOG_LAST_FILENAME;
        }
    }

    public void setBaseDir(String dir, String fileName) {
        if (dir != null && fileName != null) {
            OPLOG_LAST_FILENAME = dir + File.separator + fileName;
        }
    }

    public void setInclusions(List<String> inclusions) {
        this.inclusions = inclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }

    public void writeLastTimestamp() {
        if (lastTimestamp == null) {
            return;
        }
        Writer writer = null;
        try {
            OutputStream out = new FileOutputStream(new File(OPLOG_LAST_FILENAME));
            writer = new OutputStreamWriter(out, "UTF-8");
            String tsString = Integer.toString(lastTimestamp.getTime()) + "|" + Integer.toString(lastTimestamp.getInc());
            logger.debug("writeLastTimestamp() " + tsString);
            writer.write(tsString);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public BSONTimestamp getLastTimestamp() {
        BufferedReader input = null;
        try {
            File file = new File(OPLOG_LAST_FILENAME);
            if (!file.exists()) {
                return null;
            }
            input = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line = input.readLine();
            String[] parts = line.split("\\|");
            return new BSONTimestamp(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }
    
    private DBCursor getCursor() {
        DBCursor cursor = null;
        DBObject query = null;
        if (baseQuery == null) {
            query = new BasicDBObject();
        } else {
            query = baseQuery;
        }
        
        if (lastTimestamp != null) {
            query.put("ts", new BasicDBObject("$gt", lastTimestamp));
            cursor = oplog.find(query);
            cursor.addOption(Bytes.QUERYOPTION_OPLOGREPLAY);
        } else {
            cursor = oplog.find(query);
        }
        cursor.addOption(Bytes.QUERYOPTION_TAILABLE);
        cursor.addOption(Bytes.QUERYOPTION_AWAITDATA);
        return cursor;
    }

    @Override
    public void run() {
        running = true;
        
        try {
            lastTimestamp = getLastTimestamp();
            
            logger.debug("lastTimestamp: " + Integer.toString(lastTimestamp.getTime()) + "|" + Integer.toString(lastTimestamp.getInc()));
            

            long lastWrite = 0;
            long count = 0;
            long skips = 0;
            long startTime = System.currentTimeMillis();
            long lastOutput = System.currentTimeMillis();
            while (true) {
                try {
                    if (killMe) {
                        logger.debug("Exiting loop");
                        break;
                    }
                    DBCursor cursor = getCursor();

                    while (!killMe && cursor.hasNext()) {
                        DBObject x = cursor.next();
                        
                        if (!killMe) {
                            lastTimestamp = (BSONTimestamp) x.get("ts");
                            if (shouldWrite(x)) {
                                for (OplogRecordProcessor processor : processors)
                                    processor.processRecord((BasicDBObject) x);
                                count++;
                            } else {
                                skips++;
                            }
                            if (System.currentTimeMillis() - lastWrite > 1000) {
                                writeLastTimestamp();
                                lastWrite = System.currentTimeMillis();
                            }
                            
                            long duration = System.currentTimeMillis() - lastOutput;
                            if (duration > reportInterval) {
                                report(this.getName(), count, skips, System.currentTimeMillis() - startTime, lastTimestamp.getTime());
                                lastOutput = System.currentTimeMillis();
                            }
                        }
                    }
                } catch (com.mongodb.MongoException.CursorNotFound ex) {
                    writeLastTimestamp();
                    System.out.println("Cursor not found, waiting");
                    Thread.sleep(2000);
                } catch (com.mongodb.MongoInternalException ex) {
                    logger.warn("Cursor not found, waiting");
                    //System.out.println();
                    writeLastTimestamp();
                    //ex.printStackTrace();
                } catch (com.mongodb.MongoException ex) {
                    writeLastTimestamp();
                    System.out.println("Internal exception, waiting");
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    killMe = true;
                    writeLastTimestamp();
                    ex.printStackTrace();
                    break;
                }
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writeLastTimestamp();
            try {
                for (OplogRecordProcessor processor : processors) {
                    processor.close("oplog");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        writeLastTimestamp();
        running = false;
    }
    
    public void requestStop() {
        writeLastTimestamp();
        killMe = true;
    }
    
    

    boolean shouldWrite(DBObject obj) {
        String ns = (String) obj.get("ns");

        if (ns == null || "".equals(ns)) {
            return false;
        }
        if (exclusions.size() == 0 && inclusions.size() == 0) {
            return true;
        }
        if (exclusions.contains(ns)) {
            return false;
        }
        if (inclusions.contains(ns) || inclusions.contains("*")) {
            return true;
        }
        // check database-level inclusion
        if (ns.indexOf('.') > 0 && inclusions.contains(ns.substring(0, ns.indexOf('.')))) {
            return true;
        }

        return false;
    }

    void report(String collectionName, long count, long skips, long duration, int lastTimestamp) {
        for (OplogEventListener processor : processors) {
            processor.stats(count, skips, duration, lastTimestamp);
        }
        
        double brate = (double) count / ((duration) / 1000.0);
        if (enableOutput)
            System.out.println(collectionName + ": " + PrintFormat.LONG_FORMAT.format(count) + " records, "
                    + PrintFormat.LONG_FORMAT.format(brate) + " req/sec, " + PrintFormat.LONG_FORMAT.format(skips)
                    + " skips");
    }

    public String getBaseQueryJson() {
        return baseQueryJson;
    }

    public void setBaseQueryJson(String baseQueryJson) {
        this.baseQueryJson = baseQueryJson;
        this.baseQuery = (DBObject)JSON.parse(baseQueryJson);
    }


}
