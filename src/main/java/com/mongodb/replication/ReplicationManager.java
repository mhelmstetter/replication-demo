package com.mongodb.replication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.mongodb.replication.domain.ReplicationConfig;
import com.mongodb.replication.domain.ReplicationSource;
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
    
    private Map<ReplicationSource, List<OplogEventListener>> oplogEventListeners;
    
    public ReplicationManager() {
        oplogTailThreads = new ArrayList<OplogTailThread>();
        oplogEventListeners = new HashMap<ReplicationSource, List<OplogEventListener>>();
    }
    
    /**
     *  Creates an OplogTailThread and adds that thread to be managed by this ReplicationManager.
     *  Does not register any listeners for that thread.
     */
    public OplogTailThread createOplogTailThread() {
        OplogTailThread oplogTailThread = factory.getBean(OplogTailThread.class);
        oplogTailThreads.add(oplogTailThread);
        return oplogTailThread;
    }
    
    /**
     * Register an OplogEventListener for a given ReplicationSource
     * @param listener
     * @param replicationSource
     */
    public void registerOplogEventListener(OplogEventListener listener, ReplicationSource replicationSource) {
        List<OplogEventListener> listeners = oplogEventListeners.get(replicationSource);
        if (listeners == null) {
            listeners = new ArrayList<OplogEventListener>();
        }
        listeners.add(listener);
    }

    //@PostConstruct
    public void start() throws IOException {
        
        Iterator<ReplicationConfig> i = replicationConfigRepository.findAll().iterator();
        while (i.hasNext()) {
            ReplicationConfig config = i.next();
            logger.debug("ReplicationConfig " + config);
            
            OplogReplayWriter oplogReplayWriter = null;
            if (config.getReplicationTarget() != null) {
                oplogReplayWriter = new OplogReplayWriter(config.getReplicationTarget());
            }
            
            
            for (ReplicationSource replicationSource : config.getReplicationSources()) {
                OplogTailThread oplogTailThread = factory.getBean(OplogTailThread.class);
                logger.debug("oplogTailThread " + oplogTailThread);
                oplogTailThread.initialize(replicationSource, config);
                oplogTailThread.addOplogProcessor(oplogReplayWriter);
                
                List<OplogEventListener> listeners = oplogEventListeners.get(replicationSource);
                if (listeners != null) {
                    for (OplogEventListener listener : listeners) {
                        oplogTailThread.addOplogProcessor(listener);
                    }
                }
                
                oplogTailThread.start();
                oplogTailThreads.add(oplogTailThread);
            }
        }

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
