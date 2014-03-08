package processor;

import java.util.HashMap;
import java.util.HashSet;

import model.SearchStringToClassMapping;

import reader.DBCacheReader;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import writer.DBCacheWriter;
import writer.MongoWriter;

public class DBCacher {
	
	private MongoWriter mongoWriter;
	private DBCacheReader dbCacheReader;
	private DBCacheWriter dbCacheWriter;
	
	public DBCacher(MongoWriter mongoWriter) {
		this.mongoWriter = mongoWriter;
		this.dbCacheReader = new DBCacheReader();
		this.dbCacheWriter = new DBCacheWriter();
	}
	
	public void cacheSearchStringsToClasses() {
		
		int numProcessed = 0;
		
		DBCollection entities = this.mongoWriter.getEntitiesCollection();
		DBCollection classes = this.mongoWriter.getClassesCollection();
		DBObject entity;
		DBObject entityRelations;
		BasicDBList entityClasses;
		DBObject cls;
		DBCursor cursor;
		
		// fetch cached valid search strings
		String[] validSearchStrings = dbCacheReader.readValidSearchStrings().toArray(new String[]{});
		
		HashSet<Integer> validClasses = new HashSet<Integer>();
		HashMap<String, Integer> classNameToNIdMap = new HashMap<String, Integer>();
		
		cursor = classes.find(new BasicDBObject());
		while(cursor.hasNext()) {
			cls = cursor.next();
			classNameToNIdMap.put((String) cls.get("name"), (Integer) cls.get("nId")); 
		}
		
		// read from MongoDB
		for(String validSearchString : validSearchStrings) {
			
			cursor = entities.find(new BasicDBObject("searchString", validSearchString));
			// fetch all classes
			while(cursor.hasNext()) {
				entity = cursor.next();
				validClasses = new HashSet<Integer>(); //reset classes hash
				
				entityRelations = (DBObject) entity.get("relations");
				if(entityRelations != null) {
					entityClasses = (BasicDBList) entityRelations.get("rdf:type");
					if(entityClasses != null) {
						for(int j = 0; j < entityClasses.size(); j++) {
							validClasses.add(classNameToNIdMap.get(entityClasses.get(j)));
						}
					}
				}
			}
			
			// write to file cache
			this.dbCacheWriter.writeSearchStringToClassMappings(
					new SearchStringToClassMapping(validSearchString, validClasses.toArray(new Integer[]{}))
					);
			
			numProcessed++;
			if(numProcessed % 50000 == 0) {
				System.out.println("DBCacher: SearchStrings processed: " + numProcessed / 1000 + "k");
			}
			
		}
		
	}
	
	public void cacheValidEntitySearchStrings() {
		
		DBCursor cursor = this.mongoWriter.getEntitiesCollection().find(new BasicDBObject());
		DBObject entity;
		HashSet<String> validSearchStrings = new HashSet<String>();
		
		while(cursor.hasNext()) {
			entity = cursor.next();
			validSearchStrings.add((String)entity.get("searchString")); 
		}
		
		this.dbCacheWriter.writeValidSearchStrings(validSearchStrings.toArray(new String[]{}));
	}
	
}
