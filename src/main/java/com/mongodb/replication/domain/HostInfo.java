package com.mongodb.replication.domain;

import org.springframework.data.annotation.Id;

public class HostInfo {

    @Id
    private String id;
    
    private String hostname;
    private Integer port;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return hostname + ":" + port;
    }
    
    

}
