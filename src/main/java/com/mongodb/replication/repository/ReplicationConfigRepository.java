package com.mongodb.replication.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.mongodb.replication.domain.ReplicationConfig;

@Repository
public interface ReplicationConfigRepository extends CrudRepository<ReplicationConfig, String> {
    
}
