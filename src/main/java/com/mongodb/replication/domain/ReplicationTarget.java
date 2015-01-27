package com.mongodb.replication.domain;

import java.util.ArrayList;
import java.util.List;

public class ReplicationTarget extends HostInfo {
    
    private List<CollectionMapping> collectionMappings;
    
    private List<DatabaseMapping> databaseMappings;
    
    public ReplicationTarget() {
        collectionMappings = new ArrayList<CollectionMapping>();
        databaseMappings = new ArrayList<DatabaseMapping>();
    }

    public List<CollectionMapping> getCollectionMappings() {
        return collectionMappings;
    }

    public void setCollectionMappings(List<CollectionMapping> collectionMappings) {
        this.collectionMappings = collectionMappings;
    }
    
    public void addCollectionMapping(CollectionMapping c) {
        collectionMappings.add(c);
    }

    public List<DatabaseMapping> getDatabaseMappings() {
        return databaseMappings;
    }

    public void setDatabaseMappings(List<DatabaseMapping> databaseMappings) {
        this.databaseMappings = databaseMappings;
    }
    
    public void addDatabaseMapping(DatabaseMapping d) {
        databaseMappings.add(d);
    }

}
