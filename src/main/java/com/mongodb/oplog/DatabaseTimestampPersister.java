package com.mongodb.oplog;

import org.bson.types.BSONTimestamp;

/**
 * Read and write the lastTimestamp for an oplog using a (MongoDB) database
 * 
 */
public class DatabaseTimestampPersister extends AbstractTimestampPersister implements TimestampPersister  {
    
    @Override
    public void writeLastTimestamp(BSONTimestamp lastTimestamp) {
        if (lastTimestamp == null) {
            return;
        }
        
    }

    @Override
    public BSONTimestamp getLastTimestamp() {
        
        return null;
    }

}
