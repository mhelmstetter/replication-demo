package com.mongodb.replication.domain;

public class DatabaseMapping {

    private String sourceDatabaseName;

    private String destinationDatabaseName;
    
    public DatabaseMapping() {
    }
    
    public DatabaseMapping(String sourceDatabaseName, String destinationDatabaseName) {
        this.sourceDatabaseName = sourceDatabaseName;
        this.destinationDatabaseName = destinationDatabaseName;
    }

    public String getSourceDatabaseName() {
        return sourceDatabaseName;
    }

    public void setSourceDatabaseName(String sourceDatabaseName) {
        this.sourceDatabaseName = sourceDatabaseName;
    }

    public String getDestinationDatabaseName() {
        return destinationDatabaseName;
    }

    public void setDestinationDatabaseName(String destinationDatabaseName) {
        this.destinationDatabaseName = destinationDatabaseName;
    }

}
