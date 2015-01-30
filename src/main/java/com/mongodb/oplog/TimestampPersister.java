package com.mongodb.oplog;

import org.bson.types.BSONTimestamp;

public interface TimestampPersister {
    
    public final static String SOURCE_TARGET_ID_FORMAT = "%s-%s_%s-%s";

    public abstract void writeLastTimestamp(BSONTimestamp lastTimestamp);

    public abstract BSONTimestamp getLastTimestamp();
    
    public void setId(String id);

}