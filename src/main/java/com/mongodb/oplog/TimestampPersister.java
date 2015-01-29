package com.mongodb.oplog;

import org.bson.types.BSONTimestamp;

public interface TimestampPersister {

    public abstract void writeLastTimestamp(BSONTimestamp lastTimestamp);

    public abstract BSONTimestamp getLastTimestamp();
    
    public void setId(String id);

}