package com.mongodb.replication.domain;


public class ReplicationSourceStatus {
    
    
    private ReplicationSource replicationSource;
    
    private long count; 
    private long skips; 
    private long duration;
    private int lastTimestamp;
    
    public ReplicationSourceStatus(ReplicationSource replicationSource) {
        super();
        this.replicationSource = replicationSource;
    }
    
    
    
    public ReplicationSource getReplicationSource() {
        return replicationSource;
    }
    public void setReplicationSource(ReplicationSource replicationSource) {
        this.replicationSource = replicationSource;
    }
    public long getCount() {
        return count;
    }
    public void setCount(long count) {
        this.count = count;
    }
    public long getSkips() {
        return skips;
    }
    public void setSkips(long skips) {
        this.skips = skips;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public double getRate() {
        if (duration == 0) {
            return 0;
        }
        return (double) count / ((duration) / 1000.0);  
    }



    public void update(long count, long skips, long duration, int lastTimestamp) {
        this.count = count;
        this.skips = skips;
        this.duration = duration;
        this.lastTimestamp = lastTimestamp;
    }



    public int getLastTimestamp() {
        return lastTimestamp;
    }



    public void setLastTimestamp(int lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
   

}
