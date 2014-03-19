package model.mapping;

import java.util.HashMap;

import writer.MongoWriter;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public class ClassToEntityMap {
	
	public static final int DEFAULT_MAX_SESSIONS_PER_MAPPING = 20;
	private int maxSessionsPerMapping;
	
	// hash of className to entityName to arraylist of {SessionId, mapping strength}
	private HashMap<String, 
				HashMap<String, MapStrength
				>
			> map;
	
	public ClassToEntityMap() {
		this(DEFAULT_MAX_SESSIONS_PER_MAPPING);
	}
	
	public ClassToEntityMap(int maxSessionsPerMapping) {
		this.map = new HashMap<String, HashMap<String, MapStrength>>();
		this.maxSessionsPerMapping = maxSessionsPerMapping;
	}
	
	public void addMapping(String className, String entityName, int sessionId, double mapStrength) {
		
		HashMap<String, MapStrength> innermap;
		MapStrength mapping;
		
		if(! this.map.containsKey(className)) {
			this.map.put(className, new HashMap<String, MapStrength>());
		}
		innermap = this.map.get(className);
		if(! innermap.containsKey(entityName)) {
			innermap.put(entityName, new MapStrength(sessionId, mapStrength));
		} else {
			if(innermap.get(entityName).getMapStrength() < mapStrength) {
				innermap.put(entityName, new MapStrength(sessionId, mapStrength));
			}
		}
	}
	
	public void toDB(MongoWriter mongoWriter) {
		String[] classNames = this.map.keySet().toArray(new String[]{});
		for(String className : classNames) {
			mongoWriter.setClassToEntityMapping(className, this.map.get(className), this.maxSessionsPerMapping);
		}
	}
	
	public HashMap<String, HashMap<String, MapStrength>> getMap() {
		return this.map;
	}
	
}
