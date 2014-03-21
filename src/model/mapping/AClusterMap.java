package model.mapping;

import java.util.HashMap;

import writer.MongoWriter;

public abstract class AClusterMap {
	
	public static final int DEFAULT_MAX_SESSIONS_PER_MAPPING = 20;
	protected int maxSessionsPerMapping;
	
	// hash of className to entityName to arraylist of {SessionId, mapping strength}
	protected HashMap<String, 
				HashMap<String, MapStrength
				>
			> map;
	
	public AClusterMap() {
		this(DEFAULT_MAX_SESSIONS_PER_MAPPING);
	}
	
	public AClusterMap(int maxSessionsPerMapping) {
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
	
	public HashMap<String, HashMap<String, MapStrength>> getMap() {
		return this.map;
	}
	
	
	public abstract void toDB(MongoWriter mongoWriter);
	
}
