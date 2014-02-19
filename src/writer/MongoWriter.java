package writer;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBList;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Class handling connections to a MongoDB instance for YAGO reader output
 * 
 * @author Li Quan Khoo
 */
public class MongoWriter {
	
	private Mongo mongoClient;
	private DB db;
	private DBCollection entities;
	private DBCollection classes;
	
	private long updateCount = 0;
	private long prevTime = System.currentTimeMillis();
	private long currentTime;
	
	public MongoWriter(String host, int port, String dbName) {
		
		try {
			this.mongoClient = new Mongo(host, port);
			this.db = mongoClient.getDB(dbName);
			
			this.entities = db.getCollection("entities");
			this.classes = db.getCollection("classes");
			
			this.entities.ensureIndex(new BasicDBObject("name", 1).append("cleanName", 1));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		mongoClient.close();
	}
	
	/**
	 * Searches for the given name within the entity Mongo collection. If it doesn't exist then create it.
	 * If it exists then perform a mixin for its key value pairs
	 * @param name				Name of entity. This should be in the raw format of the YAGO files
	 * @param relationKey		Relation type. e.g. "rdf:type"
	 * @param relationValue		Value of the relation -- what's this entity related to via the given relation. Give the raw value that YAGO gives
	 */
	public void addOrUpdateEntity(String name, String relationKey, String relationValue) {
		
		DBObject document;
		BasicDBObject relation;
		BasicDBList relationList;
		
		Pattern qualifiedNamePattern = Pattern.compile("((.*)\\((.*)\\))");
		Matcher qualifiedNameMatcher;
		
		String cleanName = name.replace("_", " ").replace("<", "").replace(">", "");
		String disambig = "";
		
		qualifiedNameMatcher = qualifiedNamePattern.matcher(cleanName);
		if(qualifiedNameMatcher.find()) {
			cleanName = qualifiedNameMatcher.group(2);
			disambig = qualifiedNameMatcher.group(3);
		}
		
		BasicDBObject selector = new BasicDBObject("name", name);
		BasicDBObject insertionOperator = new BasicDBObject();
		BasicDBObject addOperator = new BasicDBObject();
		BasicDBObject setFields = new BasicDBObject();
		BasicDBObject addFields = new BasicDBObject();
		setFields.put("name", name);
		setFields.put("cleanName", cleanName);
		setFields.put("disambig", disambig);
		setFields.put("relations", new BasicDBObject());
		
		addFields.put("relations." + relationKey, relationValue);
		insertionOperator.put("$setOnInsert", setFields);
		addOperator.put("$addToSet", addFields);
		
		this.entities.update(selector, insertionOperator, true, false);
		this.entities.update(selector, addOperator, false, false);
		
		/*
		document = this.entities.findOne(new BasicDBObject("name", name));
		
		// document does not exist
		if(document == null) {
			

			

			
			relationList = new BasicDBList();
			relationList.add(relationValue);
			
			relation = new BasicDBObject();
			relation.put(relationKey, relationList);
			
			document = new BasicDBObject();
			document.put("name", name);
			document.put("cleanName", cleanName);
			document.put("disambig", disambig);
			document.put("relations", relation);
			
			this.entities.insert(document);
		} else {
			// document exists
			relation = (BasicDBObject) document.get("relations");
			relationList = (BasicDBList) relation.get(relationKey);
			
			// but relation does not
			if(relationList == null) {
				relationList = new BasicDBList();
				relationList.add(relationValue);
				relation.put(relationKey, relationList);
				
				document.put("relations", relation);
				entities.save(document);
			} else {
				
				if(! relationList.contains(relationValue)) {
					relationList.add(relationValue);
				}
				entities.save(document);
			}
		}
		*/
		
		this.updateCount++;
		if(this.updateCount % 50000 == 0) {
			this.currentTime = System.currentTimeMillis();
			int seconds = (int) Math.floor((this.currentTime - this.prevTime) / 1000);
			this.prevTime = this.currentTime;
			System.out.println("MongoWriter: " + this.updateCount / 1000 + "k transactions (" + seconds + "s)");
		}
		
	}
	
	public DBCollection getEntities() {
		return this.entities;
	}
	
	public DBCollection getClasses() {
		return this.classes;
	}
	
	public DBObject getEntity(String cleanName) {
		return this.entities.findOne(new BasicDBObject("cleanName", cleanName));
	}
	
	public void deleteEntity(String cleanName) {
		this.entities.remove(new BasicDBObject("cleanName", cleanName));
	}
	
	public void dropDatabase() {
		db.dropDatabase();
	}
	
	public void dropEntities() {
		this.entities.drop();
	}
	
	public void dropClasses() {
		this.classes.drop();
	}
	
	public long getEntityCount() {
		return this.entities.getCount();
	}
	
	public long getClassCount() {
		return this.classes.getCount();
	}
	
}
