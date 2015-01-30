package com.mongodb.replication.domain;


public class ReplicationSource extends HostInfo {
    
    private String oplogBaseQuery;

    public String getOplogBaseQuery() {
        return oplogBaseQuery;
    }

    public void setOplogBaseQuery(String oplogBaseQuery) {
        this.oplogBaseQuery = oplogBaseQuery;
    }
   

    
}
