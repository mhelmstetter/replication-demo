package com.mongodb.oplog;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.bson.BasicBSONObject;
import org.bson.types.BSONTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.replication.domain.CollectionMapping;
import com.mongodb.replication.domain.DatabaseMapping;
import com.mongodb.replication.domain.ReplicationTarget;

public class OplogReplayWriter implements OplogEventListener {
    
    protected static final Logger logger = LoggerFactory.getLogger(OplogReplayWriter.class);
    
	protected static Map<String, String> COLLECTION_MAPPING = new HashMap<String, String>();
	protected static Map<String, String> DATABASE_MAPPING = new HashMap<String, String>();
	protected static Map<String, String> NAMESPACE_COLLECTION_MAP = new HashMap<String, String>();
	protected static Map<String, String> UNMAPPED_NAMESPACE_COLLECTION_MAP = new HashMap<String, String>();

	protected long insertCount;
	protected long updateCount;
	protected long deleteCount;
	protected long commandCount;
	
	MongoClient mongoClient;
	
	public OplogReplayWriter() {
	}
	
	public OplogReplayWriter(ReplicationTarget rep) throws UnknownHostException {
	    mongoClient = new MongoClient(rep.getHostname(), rep.getPort());
	    
	    for (DatabaseMapping dbm : rep.getDatabaseMappings()) {
	        this.addDatabaseMapping(dbm.getSourceDatabaseName(), dbm.getDestinationDatabaseName());
	    }
	    
	    for (CollectionMapping cm : rep.getCollectionMappings()) {
	       this.addCollectionMapping(cm.getSourceCollectionName(), cm.getDestinationCollectionName()); 
	    }
	}
	
	public void addDatabaseMapping(String src, String dst){
		DATABASE_MAPPING.put(src, dst);
	}
	
	public void setDatabaseMappings(Map<String, String> mappings){
		DATABASE_MAPPING = mappings;
	}
	
	public void addCollectionMapping(String src, String dst){
		COLLECTION_MAPPING.put(src, dst);
	}
	
	public void setCollectionMappings(Map<String, String> mappings){
		COLLECTION_MAPPING = mappings;
	}

	public long getInsertCount() {
		return insertCount;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public long getDeleteCount() {
		return deleteCount;
	}

	public long getCommandCount() {
		return commandCount;
	}



	@Override
	public void processRecord(BasicDBObject dbo) throws Exception {
		String operationType = dbo.getString("op");
		String namespace = dbo.getString("ns");
		String targetCollection = getMappedCollectionFromNamespace(namespace);
		String targetDatabase = getDatabaseMapping(namespace);
		
		BasicDBObject operation = new BasicDBObject((BasicBSONObject)dbo.get("o"));
		
		if(shouldProcessRecord(targetDatabase, targetCollection)){
			//DB db = MongoDBConnectionManager.getConnection("REPLAY", destinationDatabaseHost, targetDatabase, destinationDatabaseUsername, destinationDatabasePassword, SchemaType.READ_WRITE());
			DB db = mongoClient.getDB(targetDatabase);
		    DBCollection coll = db.getCollection(targetCollection);
	
			try{
				if("i".equals(operationType)){
					insertCount++;
					coll.insert(operation);
				}
				else if("d".equals(operationType)){
					deleteCount++;
					coll.remove(operation);
				}
				else if("u".equals(operationType)){
					updateCount++;
					BasicDBObject o2 = new BasicDBObject((BasicBSONObject)dbo.get("o2"));
					coll.update(o2, operation);
				}
				else if("c".equals(operationType)){
					commandCount++;
					db.command(operation);
				}
			}
			catch (MongoException.DuplicateKey dke) {
			    logger.warn("Duplicate key: " + Thread.currentThread().getName() + dke.getMessage());
			}
			catch (Exception e) {
			    BSONTimestamp ts = (BSONTimestamp) dbo.get("ts");
			    logger.error("Error processing record", e);
			}
		}
	}

	protected boolean shouldProcessRecord(String database, String collection){
		if(database != null && collection != null){
			return true;
		}
		return false;
	}
	
	public String getUnmappedCollectionFromNamespace(String namespace) {
		if(UNMAPPED_NAMESPACE_COLLECTION_MAP.containsKey(namespace)){
			return UNMAPPED_NAMESPACE_COLLECTION_MAP.get(namespace);
		}
		String[] parts = namespace.split("\\.");
		if(parts == null || parts.length == 1){
			return null;
		}
		String collection = null;
		if(parts.length == 2){
			collection = parts[1];
		}
		else{
			collection = namespace.substring(parts[0].length()+1);
		}
		
		UNMAPPED_NAMESPACE_COLLECTION_MAP.put(namespace, collection);

		return collection;
	}

	/**
	 * returns a collection name from FQ namespace.  Assumes database name never has "." in it.
	 * 
	 * @param namespace
	 * @return
	 */
	public String getMappedCollectionFromNamespace(String namespace) {
		if(NAMESPACE_COLLECTION_MAP.containsKey(namespace)){
			return NAMESPACE_COLLECTION_MAP.get(namespace);
		}
		String[] parts = namespace.split("\\.");
		if(parts == null || parts.length == 1){
			return null;
		}
		String collection = null;
		if(parts.length == 2){
			collection = parts[1];
		}
		else{
			collection = namespace.substring(parts[0].length()+1);
		}
		
		collection = remapCollection(collection);
		NAMESPACE_COLLECTION_MAP.put(namespace, collection);

		return collection;
	}

	/**
	 * remaps a collection if mapping exists, returns original if not
	 * 
	 * @param collection
	 * @return
	 */
	public String remapCollection(String collection){
		String o = COLLECTION_MAPPING.get(collection);
		return o == null ? collection:o;
	}

	/**
	 * returns a database name from FQ namespace.  Assumes database name never has "." in it.
	 * 
	 * @param namespace
	 * @return
	 */
	public String getDatabaseMapping (String namespace) {
		String[] parts = namespace.split("\\.");
		if(parts == null || parts.length == 1){
			return null;
		}
		String databaseName = parts[0];
		databaseName = remapDatabase(databaseName);
		return databaseName;
	}

	/**
	 * remaps a database name if mapping exists, returns original if not
	 * 
	 * @param databaseName
	 * @return
	 */
	public String remapDatabase(String databaseName){
		String o = DATABASE_MAPPING.get(databaseName);
		return o == null ? databaseName:o;
	}

	@Override
	public void close(String string) throws IOException {}

    @Override
    public void stats(long count, long skips, long duration, int lastTimestamp) {
        // TODO Auto-generated method stub
        
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
}
