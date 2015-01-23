package com.mongodb.replication.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.mongodb.replication.domain.ReplicationSource;

@Repository
public interface ReplicationSourceRepository extends CrudRepository<ReplicationSource, String> {
    
}
