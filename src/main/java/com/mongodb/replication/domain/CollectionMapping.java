package com.mongodb.replication.domain;

public class CollectionMapping {
    
    private String sourceCollectionName;
    private String destinationCollectionName;
    
    public CollectionMapping() {
    }
    
    public CollectionMapping(String sourceCollectionName, String destinationCollectionName) {
        this.sourceCollectionName = sourceCollectionName;
        this.destinationCollectionName = destinationCollectionName;
    }
    
    public String getSourceCollectionName() {
        return sourceCollectionName;
    }
    public void setSourceCollectionName(String sourceCollectionName) {
        this.sourceCollectionName = sourceCollectionName;
    }
    public String getDestinationCollectionName() {
        return destinationCollectionName;
    }
    public void setDestinationCollectionName(String destinationCollectionName) {
        this.destinationCollectionName = destinationCollectionName;
    }
    
    

}
