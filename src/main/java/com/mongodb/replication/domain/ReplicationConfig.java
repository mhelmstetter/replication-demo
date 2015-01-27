package com.mongodb.replication.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ReplicationConfig {
    
    @Id
    private Long id;
    
    private List<ReplicationSource> replicationSources;
    
    private ReplicationTarget replicationTarget;
    
    public ReplicationConfig() {
        replicationSources = new ArrayList<ReplicationSource>();
    }

    public List<ReplicationSource> getReplicationSources() {
        return replicationSources;
    }

    public void setReplicationSources(List<ReplicationSource> replicationSources) {
        this.replicationSources = replicationSources;
    }
    
    public void addReplicationSource(ReplicationSource replicationSource) {
        replicationSources.add(replicationSource);
    }

    public ReplicationTarget getReplicationTarget() {
        return replicationTarget;
    }

    public void setReplicationTarget(ReplicationTarget replicationDestination) {
        this.replicationTarget = replicationDestination;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    

}
