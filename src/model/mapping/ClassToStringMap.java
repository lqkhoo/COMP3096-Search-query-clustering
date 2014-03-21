package model.mapping;

import java.util.HashMap;

import writer.MongoWriter;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public class ClassToStringMap {
	
	public static final int DEFAULT_MAX_SESSIONS_PER_MAPPING = 20;
	private int maxSessionsPerMapping;
	
	// hash of className to entityName to arraylist of {SessionId, mapping strength}
	private HashMap<String, 
				HashMap<String, Integer
				>
			> map;
	
	public ClassToStringMap() {
		this(DEFAULT_MAX_SESSIONS_PER_MAPPING);
	}
	
	public ClassToStringMap(int maxSessionsPerMapping) {
		this.map = new HashMap<String, HashMap<String, Integer>>();
		this.maxSessionsPerMapping = maxSessionsPerMapping;
	}
	
	public void addMapping(String className, String entityName, int sessionId, int mapStrength) {
		
		HashMap<String, Integer> innermap;
		Integer mapping;
		
		if(! this.map.containsKey(className)) {
			this.map.put(className, new HashMap<String, Integer>());
		}
		innermap = this.map.get(className);
		if(! innermap.containsKey(entityName)) {
			innermap.put(entityName, mapStrength);
		} else {
			innermap.put(entityName, innermap.get(entityName) + mapStrength);
		}
	}
	
	public void toDB(MongoWriter mongoWriter) {
		String[] entityNames = this.map.keySet().toArray(new String[]{});
		for(String entityName : entityNames) {
			mongoWriter.setClassToStringMapping(entityName, this.map.get(entityName), maxSessionsPerMapping);
		}
	}
	
	public HashMap<String, HashMap<String, Integer>> getMap() {
		return this.map;
	}
	
}
