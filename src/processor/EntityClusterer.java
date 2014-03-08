package processor;

import java.util.ArrayList;
import java.util.HashMap;

import processor.yago.YagoSimpleTypesProcessor;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import writer.MongoWriter;

/**
 * 
 * This class will extract relations out of the "entities" mongoDB collection
 * and map their respective entities to them, so this gives us the mappings
 * 
 * class -> [entity] (this class' output)
 * as well as 
 * entity -> [class] (YagoProcessor's output)
 * 
 * Output of the class is to the "classes" MongoDB collection
 * 
 * @author Li Quan Khoo
 */
public class EntityClusterer {
	
	private MongoWriter mongoWriter;
	
	private int updateCount;
	private long prevTime = System.currentTimeMillis();
	private long currentTime;
	
	public EntityClusterer(MongoWriter mongoWriter) {
		this.mongoWriter = mongoWriter;
		this.updateCount = 0;
	}
	
	/**
	 * This maps all entities to their leaf categories, by using using the mapping in the original yagoSimpleTypes file
	 */
	public void mapLeaves(YagoSimpleTypesProcessor processor) {
		System.out.println("EntityClusterer: Processing...");
		processor.processClassesTsv();
	}
	
	/**
	 * This will map all entities to either all categories or to their leaf categories
	 * 
	 * This performs the mapping in memory, otherwise it runs for days due to I/O. Takes about 1-2Gb or RAM (maximum)
	 */
	public void mapAll() {
		
		DBCollection entities;
		DBCursor cursor;
		DBObject entity;
		
		BasicDBObject relations;
		BasicDBList rdfTypes;
		String[] classes;
		
		String entityName;
		
		entities = mongoWriter.getEntitiesCollection();
		cursor = entities.find(new BasicDBObject());
		
		System.out.println("EntityClusterer: Processing...");
		
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		
		try {
			while(cursor.hasNext()) {
				entity = cursor.next();
				entityName = (String) entity.get("name");
				
				if(entity.containsField("relations")) {
					relations = (BasicDBObject) entity.get("relations");
					if(relations.containsField("rdf:type")) {						
						rdfTypes = (BasicDBList) relations.get("rdf:type");
						classes = rdfTypes.toArray(new String[]{});
						for(String className : classes) {
							if(! map.containsKey(className)) {
								map.put(className, new ArrayList<String>());
							}
							map.get(className).add(entityName);
						}
					}
				}
				
				this.updateCount++;
				
				if(this.updateCount % 10000 == 0) {
					this.currentTime = System.currentTimeMillis();
					int seconds = (int) Math.floor((this.currentTime - this.prevTime) / 1000);
					this.prevTime = this.currentTime;
					System.out.println("EntityClusterer: " + this.updateCount / 1000 + "k entities processed (" + seconds + "s)");
				}
			}
			
			// Currently this does not write to MongoDB! Set write operations here if we need that 
			
			System.out.println(map.keySet().size());
			
		} finally {
			mongoWriter.close();
		}
	}
	
}
