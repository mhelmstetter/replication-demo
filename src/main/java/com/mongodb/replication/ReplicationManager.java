package com.mongodb.replication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.mongodb.oplog.OplogEventListener;
import com.mongodb.oplog.OplogReplayWriter;
import com.mongodb.oplog.OplogTailThread;
import com.mongodb.replication.domain.DatabaseMapping;
import com.mongodb.replication.domain.ReplicationConfig;
import com.mongodb.replication.domain.ReplicationSource;
import com.mongodb.replication.domain.ReplicationTarget;
import com.mongodb.replication.repository.ReplicationConfigRepository;

@Component
@Lazy
public class ReplicationManager {
    
    protected static final Logger logger = LoggerFactory.getLogger(ReplicationManager.class);

    @Autowired
    private ReplicationConfigRepository replicationConfigRepository;
    
    @Autowired
    BeanFactory factory;
    
    private List<OplogTailThread> oplogTailThreads;
    
    private List<OplogEventListener> oplogEventListeners;
    
    public ReplicationManager() {
        oplogTailThreads = new ArrayList<OplogTailThread>();
        oplogEventListeners = new ArrayList<OplogEventListener>();
    }
    
    private void initializeRegionToWorld() {
        ReplicationConfig regionToWorld = new ReplicationConfig();
        regionToWorld.setId(1L);
        ReplicationSource source1 = new ReplicationSource();
        source1.setHostname("localhost");
        source1.setPort(37017);
        source1.setOplogBaseQuery("{ns:'region.flightTrack', 'o.airline':{$exists:true}}");
        regionToWorld.addReplicationSource(source1);
        
        ReplicationTarget replicationTarget = new ReplicationTarget();
        replicationTarget.setHostname("localhost");
        replicationTarget.setPort(27017);
        replicationTarget.addDatabaseMapping(new DatabaseMapping("region", "world"));
        regionToWorld.setReplicationTarget(replicationTarget);
        
        replicationConfigRepository.save(regionToWorld);
    }
    
    private void initializeWorldToRegion(Long id, String region, int port) {
        ReplicationConfig worldToRegion = new ReplicationConfig();
        worldToRegion.setId(id);
        ReplicationSource worldSource = new ReplicationSource();
        worldSource.setHostname("localhost");
        worldSource.setPort(27017);
        worldSource.setOplogBaseQuery(String.format("{ns:'world.flightTrack', 'o.region':{$ne:'%s'}}", region));
        worldToRegion.addReplicationSource(worldSource);
        
        ReplicationTarget replicationTarget = new ReplicationTarget();
        replicationTarget.setHostname("localhost");
        replicationTarget.setPort(port);
        worldToRegion.setReplicationTarget(replicationTarget);
        
        replicationConfigRepository.save(worldToRegion);
    }

    //@PostConstruct
    public void start() throws IOException {
        
        //initializeRegionToWorld();
        //initializeWorldToRegion(2L, "east", 37017);
        //initializeWorldToRegion(3L, "west", 47017);

        Iterator<ReplicationConfig> i = replicationConfigRepository.findAll().iterator();
        while (i.hasNext()) {
            ReplicationConfig config = i.next();
            logger.debug("ReplicationConfig " + config);
            
            OplogReplayWriter oplogReplayWriter = new OplogReplayWriter(config.getReplicationTarget());
            oplogEventListeners.add(oplogReplayWriter);
            
            
            for (ReplicationSource replicationSource : config.getReplicationSources()) {
                //OplogTailThread oplogTailThread = new OplogTailThread(replicationSource);
                OplogTailThread oplogTailThread = factory.getBean(OplogTailThread.class);
                logger.debug("oplogTailThread " + oplogTailThread);
                oplogTailThread.setReplicationSource(replicationSource, config);
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
