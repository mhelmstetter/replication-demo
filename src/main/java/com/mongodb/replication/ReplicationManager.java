package com.mongodb.replication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mongodb.oplog.OplogEventListener;
import com.mongodb.oplog.ReplicationUtil;
import com.mongodb.replication.domain.ReplicationSource;
import com.mongodb.replication.domain.ReplicationSourceStatus;
import com.mongodb.replication.repository.ReplicationSourceRepository;

@Component
@Lazy
public class ReplicationManager {
    
    protected static final Logger logger = LoggerFactory.getLogger(ReplicationManager.class);

    @Autowired
    private ReplicationSourceRepository replicationSourceRepository;
    
    // A local cache of all replication sources
    private List<ReplicationSource> replicationSources;
    private List<ReplicationUtil> replicationUtils;
    
    @Autowired
    OplogEventListener processor;

    //@PostConstruct
    public void start() throws IOException {

        replicationSources = new ArrayList<ReplicationSource>();
        replicationUtils = new ArrayList<ReplicationUtil>();
        
        // ReplicationSource source1 = new ReplicationSource();
        // source1.setHostname("localhost");
        // source1.setPort(37017);
        // replicationSourceRepository.save(source1);
        //
        // ReplicationSource source2 = new ReplicationSource();
        // source2.setHostname("localhost");
        // source2.setPort(47017);
        // replicationSourceRepository.save(source2);

        Iterator<ReplicationSource> i = replicationSourceRepository.findAll().iterator();
        while (i.hasNext()) {
            ReplicationSource replicationSource = i.next();
            logger.debug("Initializing " + replicationSource);
            ReplicationSourceStatus replicationStatus = new ReplicationSourceStatus(replicationSource);

            ReplicationUtil util = new ReplicationUtil(replicationStatus, "localhost", 27017, processor);
            replicationUtils.add(util);
        }

    }
    
    @PreDestroy
    public void shutdown() {
        logger.debug("******* Starting shutdown");
        for (ReplicationUtil util : replicationUtils) {
            util.requestStop();
        }
        logger.debug("******* Finished shutdown");
    }

}
