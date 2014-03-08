package writer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Class handling connections to a MongoDB instance, and operations on it
 * 
 * @author Li Quan Khoo
 */
public class MongoWriter {
	
	private Mongo mongoClient;
	private DB db;
	private DBCollection entities;
	private DBCollection classes;
	private DBCollection classMemberArrays;
	@Deprecated
	private DBCollection searchMaps;
	private DBCollection usefulSessions;
	
	private long updateCount = 0;
	private long prevTime = System.currentTimeMillis();
	private long currentTime;
	
	public MongoWriter(String host, int port, String dbName) {
		
		try {
			this.mongoClient = new Mongo(host, port);
			this.db = mongoClient.getDB(dbName);
			
			/* This collection stores Yago2 entities */
			this.entities = db.getCollection("entities");
			this.entities.ensureIndex(new BasicDBObject("name", 1));
			this.entities.ensureIndex(new BasicDBObject("cleanName", 1));
			this.entities.ensureIndex(new BasicDBObject("searchString", 1));
			
			/* This collection stores Yago2 class hierarchy information and a list of id references to the classMemberArrays collection */
			this.classes = db.getCollection("classes");
			this.classes.ensureIndex(new BasicDBObject("name", 1));
			this.classes.ensureIndex(new BasicDBObject("nId", 1));
			
			/* This collection stores entity class membership information within arrays to work around the 16Mb mongodb document size limit */
			this.classMemberArrays = db.getCollection("classMemberArrays");
			this.classMemberArrays.ensureIndex(new BasicDBObject("id", 1));
			
			/* This collection stores string to string query clusters */
			this.searchMaps = db.getCollection("searchMaps");
			this.searchMaps.ensureIndex(new BasicDBObject("searchString", 1));
			
			/* These are AOL log search sessions mapping to more than one valid searchString */
			this.usefulSessions = db.getCollection("usefulSessions");
			this.usefulSessions.ensureIndex(new BasicDBObject("sessionId", 1));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	// close
	public void close() {
		mongoClient.close();
	}
	
	// Update methods
	/**
	 * Searches for the given name within the entity Mongo collection. If it doesn't exist then create it.
	 * If it exists then perform a mixin for its key value pairs
	 * @param name				Name of entity. This should be in the raw format of the YAGO files
	 * @param relationKey		Relation type. e.g. "rdf:type"
	 * @param relationValue		Value of the relation -- what's this entity related to via the given relation. Give the raw value that YAGO gives
	 */
	public void addOrUpdateEntity(String name, String relationKey, String relationValue) {
		
		Pattern qualifiedNamePattern = Pattern.compile("(^(.*)[ ]\\((.*)\\)$)");
		Matcher qualifiedNameMatcher;
		
		String cleanName = name.replace("_", " ").replace("<", "").replace(">", "");
		String searchString;
		String disambig = "";
		
		qualifiedNameMatcher = qualifiedNamePattern.matcher(cleanName);
		if(qualifiedNameMatcher.find()) {
			cleanName = qualifiedNameMatcher.group(2);
			disambig = qualifiedNameMatcher.group(3);
		}
		searchString = cleanName.toLowerCase();
		
		addOrUpdateEntity(name, cleanName, disambig, searchString, relationKey, relationValue);
	}
	
	/**
	 * Custom entity formatting and entry. Currently used for entering wordnet entities, which have different formatting
	 * @param name
	 * @param cleanName
	 * @param disambig
	 * @param searchString
	 * @param relationKey
	 * @param relationValue
	 */
	public void addOrUpdateEntity(String name, String cleanName, String disambig,
			String searchString, String relationKey, String relationValue) {
		BasicDBObject selector = new BasicDBObject("name", name);
		BasicDBObject insertionOperator = new BasicDBObject();
		BasicDBObject addOperator = new BasicDBObject();
		BasicDBObject setFields = new BasicDBObject();
		BasicDBObject addFields = new BasicDBObject();
		setFields.put("name", name);
		setFields.put("cleanName", cleanName);
		setFields.put("searchString", searchString);
		setFields.put("disambig", disambig);
		setFields.put("relations", new BasicDBObject());
		
		addFields.put("relations." + relationKey, relationValue);
		insertionOperator.put("$setOnInsert", setFields);
		addOperator.put("$addToSet", addFields);
		
		this.entities.update(selector, insertionOperator, true, false);
		this.entities.update(selector, addOperator, false, false);
		
		this.updateCount++;

		if(this.updateCount % 50000 == 0) {
			this.currentTime = System.currentTimeMillis();
			int seconds = (int) Math.floor((this.currentTime - this.prevTime) / 1000);
			this.prevTime = this.currentTime;
			System.out.println("MongoWriter: " + this.updateCount / 1000 + "k transactions (" + seconds + "s)");
		}
	}
	
	public void addOrUpdateSearchMap(String searchString, String[] searchStrings, HashMap<String, Boolean> searchStringsHash, int sessionId) {
		
		BasicDBObject selector = new BasicDBObject("searchString", searchString);
		BasicDBObject setOnInsertOperator = new BasicDBObject();
		BasicDBObject addToSetOperator = new BasicDBObject();
		
		// initial set
		BasicDBObject mappings = new BasicDBObject();
		
		BasicDBObject setOnInsertFields = new BasicDBObject();
		setOnInsertFields.put("searchString", searchString);
		setOnInsertFields.put("mappings", mappings);
		
		// add to set on update
		BasicDBObject addToSetFields = new BasicDBObject();
		
		for(String entityName : searchStrings) {
			
			// let entity map to self, as sessionId won't be registered for single-entity sessions otherwise
			/*
			if(entityName.equals(searchString)) {
				continue;
			} else {
				addToSetFields.put("mappings." + entityName, sessionId);
			}
			*/
			addToSetFields.put("mappings." + entityName, sessionId);
		}
		
		// finalize operator
		setOnInsertOperator.put("$setOnInsert", setOnInsertFields);
		addToSetOperator.put("$addToSet", addToSetFields);
		
		// exec
		this.searchMaps.update(selector, setOnInsertOperator, true, false);
		this.searchMaps.update(selector, addToSetOperator, false, false);
		
	}
	
	/**
	 * This is the method called by EntityClusterer, as it does not have class information available
	 * @param className
	 * @param entityName
	 */
	public void setClassMembers(String className, String[] entityNameArray, int entityNameArrayId) {
		BasicDBObject selector = new BasicDBObject("name", className);
		BasicDBObject setOnInsertOperator = new BasicDBObject();
		BasicDBObject addToSetOperator = new BasicDBObject();
		BasicDBObject setOnInsertFields = new BasicDBObject();
		BasicDBObject pushFields = new BasicDBObject();
		
		setOnInsertFields.put("name", className);
		setOnInsertFields.put("superclassNames", new BasicDBList());
		setOnInsertFields.put("subclassNames", new BasicDBList());
		setOnInsertFields.put("memberArrayIds", new BasicDBList());
		
		pushFields.put("memberArrayIds", entityNameArrayId);
		
		setOnInsertOperator.put("$setOnInsert", setOnInsertFields);
		addToSetOperator.put("$addToSet", pushFields);
		
		this.classes.update(selector, setOnInsertOperator, true, false);
		this.classes.update(selector, addToSetOperator, false, false);
		
		BasicDBObject arraySelector = new BasicDBObject("id", entityNameArrayId);
		BasicDBObject arraySetOperator = new BasicDBObject();
		BasicDBObject arraySetFields = new BasicDBObject();
		
		arraySetFields.put("id", entityNameArrayId);
		arraySetFields.put("members", entityNameArray);
		arraySetOperator.put("$setOnInsert", arraySetFields);
		
		this.classMemberArrays.update(arraySelector, arraySetOperator, true, false);
		
		this.updateCount++;
		
		if(this.updateCount % 10000 == 0) {
			this.currentTime = System.currentTimeMillis();
			int seconds = (int) Math.floor((this.currentTime - this.prevTime) / 1000);
			this.prevTime = this.currentTime;
			System.out.println("MongoWriter: " + this.updateCount / 1000 + "k transactions (" + seconds + "s)");
		}
	}
	
	/**
	 * This is the method called by YagoHierarchy if writing to MongoDB, as it does not have entity membership information
	 * @param className
	 * @param superclassNames
	 * @param subclassNames
	 */
	public void setClassHierarchy(String className, String[] superclassNames, String[] subclassNames) {
		
		BasicDBObject selector = new BasicDBObject("name", className);
		BasicDBObject setOnInsertOperator = new BasicDBObject();
		BasicDBObject setOperator = new BasicDBObject();
		BasicDBObject setOnInsertFields = new BasicDBObject();
		BasicDBObject setFields = new BasicDBObject();
		
		setOnInsertFields.put("name", className);
		setOnInsertFields.put("superclassNames", new BasicDBList());
		setOnInsertFields.put("subclassNames", new BasicDBList());
		setOnInsertFields.put("memberArrayIds", new BasicDBList());
		
		setFields.put("superclassNames", superclassNames);
		setFields.put("subclassNames", subclassNames);
		
		setOnInsertOperator.put("$setOnInsert", setOnInsertFields);
		setOperator.put("$set", setFields);
		
		this.classes.update(selector, setOnInsertOperator, true, false);
		this.classes.update(selector, setOperator, false, false);
		
		this.updateCount++;
		
		if(this.updateCount % 10000 == 0) {
			this.currentTime = System.currentTimeMillis();
			int seconds = (int) Math.floor((this.currentTime - this.prevTime) / 1000);
			this.prevTime = this.currentTime;
			System.out.println("MongoWriter: " + this.updateCount / 1000 + "k transactions (" + seconds + "s)");
		}
	}
	
	public void setClassNId(String className, int nId) {
		BasicDBObject selector = new BasicDBObject("name", className);
		BasicDBObject setOperator = new BasicDBObject();
		BasicDBObject setFields = new BasicDBObject();
		
		setFields.put("nId", nId);
		setOperator.put("$set", setFields);
		
		this.classes.update(selector, setOperator, false, false);
	}
	
	/**
	 * Adds or updates a document in the "classes" collection
	 * This method is very slow compared to the above two, which build all information in RAM and writes in one go
	 * 
	 * @param className
	 * @param superclassNames
	 * @param subclassNames
	 * @param memberEntityNames
	 */
	@Deprecated
	public void addOrSetClass(String className, String[] superclassNames, String[] subclassNames, String[] memberEntityNames) {
		BasicDBObject selector = new BasicDBObject("name", className);
		BasicDBObject insertionOperator = new BasicDBObject();
		BasicDBObject addOperator = new BasicDBObject();
		BasicDBObject setFields = new BasicDBObject();
		BasicDBObject addFields = new BasicDBObject();
		
		setFields.put("name", className);
		setFields.put("superclassNames", new BasicDBList());
		setFields.put("subclassNames", new BasicDBList());
		setFields.put("members", new BasicDBList());
		
		if(superclassNames.length != 0) {
			addFields.put("superclassNames", new BasicDBObject("$each", superclassNames));
		}
		if(subclassNames.length != 0) {
			addFields.put("subclassNames", new BasicDBObject("$each", subclassNames));
		}
		
		if(memberEntityNames.length != 0) {
			addFields.put("members", new BasicDBObject("$each", memberEntityNames));
		}
		
		insertionOperator.put("$setOnInsert", setFields);
		addOperator.put("$addToSet", addFields);
		
		this.classes.update(selector, insertionOperator, true, false);
		this.classes.update(selector, addOperator, false, false);
		
		this.updateCount++;
		
		if(this.updateCount % 10000 == 0) {
			this.currentTime = System.currentTimeMillis();
			int seconds = (int) Math.floor((this.currentTime - this.prevTime) / 1000);
			this.prevTime = this.currentTime;
			System.out.println("MongoWriter: " + this.updateCount / 1000 + "k transactions (" + seconds + "s)");
		}
	}
	
	public void addOrUpdateUsefulSession(int sessionId, String[] searchStrings) {
		BasicDBObject selector = new BasicDBObject("sessionId", sessionId);
		BasicDBObject setOperator = new BasicDBObject();
		BasicDBObject setFields = new BasicDBObject();
		
		setFields.put("sessionId", sessionId);
		setFields.put("searchStrings", searchStrings);
		setOperator.put("$set", setFields);
		
		this.usefulSessions.update(selector, setOperator, true, false);
	}
	
	/*
	 
	// for QueryMapperOld
	public void addOrUpdateSearchMap(String name, String[] entityNames, HashMap<String, Boolean> entityNamesHash, int sessionId) {
				
		BasicDBObject selector = new BasicDBObject("name", name);
		BasicDBObject setOnInsertOperator = new BasicDBObject();
		BasicDBObject addToSetOperator = new BasicDBObject();
		
		// initial set
		BasicDBObject mappings = new BasicDBObject();
		mappings.put("complete", new BasicDBObject());
		mappings.put("partial", new BasicDBObject());
		
		BasicDBObject setOnInsertFields = new BasicDBObject();
		setOnInsertFields.put("name", name);
		setOnInsertFields.put("mappings", mappings);
		
		// add to set on update
		BasicDBObject addToSetFields = new BasicDBObject();
		
		for(String entityName : entityNames) {
			String matchField;
			if(entityName.equals(name)) {
				continue;
			} else {
				matchField = (entityNamesHash.get(name) == true) ? "complete" : "partial";
				addToSetFields.put("mappings." + matchField + "." + entityName, sessionId);
			}
		}
		
		// finalize operator
		setOnInsertOperator.put("$setOnInsert", setOnInsertFields);
		addToSetOperator.put("$addToSet", addToSetFields);
		
		// exec
		this.searchMaps.update(selector, setOnInsertOperator, true, false);
		this.searchMaps.update(selector, addToSetOperator, false, false);
		
		incrementUpdateCountAndReport();
	}
	*/
	
	// Collection methods
	public DBCollection getEntitiesCollection() {
		return this.entities;
	}
	
	public DBCollection getClassesCollection() {
		return this.classes;
	}
	
	public DBCollection getClassMemberArraysCollection() {
		return this.classMemberArrays;
	}
	
	@Deprecated
	public DBCollection getSearchMapsCollection() {
		return this.searchMaps;
	}
	
	public DBCollection getUsefulSessionsCollection() {
		return this.usefulSessions;
	}
	
	// Document methods
	public DBObject getOneEntity(BasicDBObject criteria) {
		return this.entities.findOne(criteria);
	}
	
	public DBObject getUniqueEntity(String entityName) {
		return this.entities.findOne(new BasicDBObject("name", entityName));
	}
	
	public ArrayList<DBObject> getEntities(BasicDBObject criteria) {
		ArrayList<DBObject> items = new ArrayList<DBObject>();
		DBCursor cursor = this.entities.find(criteria);
		try {
			while(cursor.hasNext()) {
				items.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		return items;
	}
	
	public void deleteEntity(String cleanName) {
		this.entities.remove(new BasicDBObject("cleanName", cleanName));
	}
	
	public DBObject getClass(String className) {
		return this.getClassesCollection().findOne(new BasicDBObject("name", className));
	}
	
	public String[] getClassMembers(String className) {
		ArrayList<String> members = new ArrayList<String>();
		
		ArrayList<Integer> memberArrayIds = new ArrayList<Integer>();
		for(Object id : ((BasicDBList) this.getClassesCollection().findOne(new BasicDBObject("name", className)).get("memberArrayIds"))) {
			memberArrayIds.add((int) id);
		}
		
		for(int id : memberArrayIds) {
			for(Object entity : ((BasicDBList) this.getClassMemberArraysCollection().findOne(new BasicDBObject("id", id)).get("members"))) {
				members.add((String) entity);
			}
		}
		return members.toArray(new String[]{});
	}
	
	
	// Deletion methods
	public void dropDatabase() {
		db.dropDatabase();
	}
	
	public void dropEntitiesCollection() {
		this.entities.drop();
	}
	
	public void dropClassesCollection() {
		this.classes.drop();
	}
	
	
	// Count methods
	public long getEntitiesCollectionCount() {
		return this.entities.getCount();
	}
	
	public long getClassesCollectionCount() {
		return this.classes.getCount();
	}
		
}
