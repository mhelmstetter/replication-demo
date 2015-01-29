package com.mongodb.oplog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTimestampPersister  implements TimestampPersister {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractTimestampPersister.class);
    
    private String id;

    public AbstractTimestampPersister() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}