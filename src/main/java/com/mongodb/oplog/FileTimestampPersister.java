package com.mongodb.oplog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.bson.types.BSONTimestamp;

/**
 * Read and write the lastTimestamp for an oplog using a simple text file.
 * The id property is used to ensure uniqueness of the text file(s).
 * 
 */
public class FileTimestampPersister extends AbstractTimestampPersister implements TimestampPersister {
    
    private String filename;
    
    private String getFilename() {
        if (filename == null) {
            return "last_timestamp_" + this.getId() + ".txt";
        }
        return filename;
    }
    
    @Override
    public void writeLastTimestamp(BSONTimestamp lastTimestamp) {
        if (lastTimestamp == null) {
            return;
        }
        Writer writer = null;
        try {
            OutputStream out = new FileOutputStream(new File(getFilename()));
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

    @Override
    public BSONTimestamp getLastTimestamp() {
        BufferedReader input = null;
        try {
            File file = new File(getFilename());
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

}
