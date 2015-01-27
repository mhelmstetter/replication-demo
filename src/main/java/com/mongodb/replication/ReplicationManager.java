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
import com.mongodb.oplog.OplogReplayWriter;
import com.mongodb.oplog.OplogTailThread;
import com.mongodb.replication.domain.DatabaseMapping;
import com.mongodb.replication.domain.ReplicationConfig;
import com.mongodb.replication.domain.ReplicationTarget;
import com.mongodb.replication.domain.ReplicationSource;
import com.mongodb.replication.repository.ReplicationConfigRepository;

@Component
@Lazy
public class ReplicationManager {
    
    protected static final Logger logger = LoggerFactory.getLogger(ReplicationManager.class);

    @Autowired
    private ReplicationConfigRepository replicationConfigRepository;
    
    private List<OplogTailThread> oplogTailThreads;
    
    private List<OplogEventListener> oplogEventListeners;
    
    public ReplicationManager() {
        oplogTailThreads = new ArrayList<OplogTailThread>();
        oplogEventListeners = new ArrayList<OplogEventListener>();
    }
    
    public void initializeTestConfig() {
        ReplicationConfig config = new ReplicationConfig();
        config.setId(1L);
        ReplicationSource source1 = new ReplicationSource();
        source1.setHostname("localhost");
        source1.setPort(37017);
        source1.setOplogBaseQuery("{'o.airline':{$exists:true}}");
        config.addReplicationSource(source1);
        
        ReplicationTarget replicationTarget = new ReplicationTarget();
        replicationTarget.setHostname("localhost");
        replicationTarget.setPort(27017);
        replicationTarget.addDatabaseMapping(new DatabaseMapping("region", "world"));
        config.setReplicationTarget(replicationTarget);
        
        replicationConfigRepository.save(config);
    }

    //@PostConstruct
    public void start() throws IOException {
        
        initializeTestConfig();

         
        // replicationSourceRepository.save(source1);
        //
        // ReplicationSource source2 = new ReplicationSource();
        // source2.setHostname("localhost");
        // source2.setPort(47017);
        // replicationSourceRepository.save(source2);

        Iterator<ReplicationConfig> i = replicationConfigRepository.findAll().iterator();
        while (i.hasNext()) {
            ReplicationConfig config = i.next();
            logger.debug("ReplicationConfig " + config);
            
            OplogReplayWriter oplogReplayWriter = new OplogReplayWriter(config.getReplicationTarget());
            oplogEventListeners.add(oplogReplayWriter);
            
            
            for (ReplicationSource replicationSource : config.getReplicationSources()) {
                OplogTailThread oplogTailThread = new OplogTailThread(replicationSource);
                for (OplogEventListener listener : oplogEventListeners) {
                    oplogTailThread.addOplogProcessor(listener);
                }
                oplogTailThread.start();
                oplogTailThreads.add(oplogTailThread);
            }
        }

    }
    
    public void addOplogEventListener(OplogEventListener listener) {
        oplogEventListeners.add(listener);
    }
    
    @PreDestroy
    public void shutdown() {
        logger.debug("******* Starting shutdown");
        for (OplogTailThread thread : oplogTailThreads) {
            thread.requestStop();
        }
        logger.debug("******* Finished shutdown");
    }

}
