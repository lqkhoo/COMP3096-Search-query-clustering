package model.mapping;

import java.util.ArrayList;
import java.util.Collections;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * Container object.
 * Model of a yago entity mapping to a semanticSession object
 * 
 * @author Li Quan Khoo
 */
public class EntityToClassMapping {
	
	public final String entityName;
	public final String searchString;
	private ArrayList<MapStrength> mappings;
	
	public EntityToClassMapping(String entityName, String searchString) {
		this.entityName = entityName;
		this.searchString = searchString;
		this.mappings = new ArrayList<MapStrength>();
	}
	
	public MapStrength addMapping(int sessionId, double similarity) {
		MapStrength mapping = new MapStrength(sessionId, similarity);
		this.mappings.add(mapping);
		return mapping;
	}
	
	public void sortMappings() {
		Collections.sort(this.mappings);
	}
	
	public BasicDBList mappingsAsBasicDBList() {
		BasicDBList ls = new BasicDBList();
		BasicDBObject mapObj;
		MapStrength mapStrength;
		
		for(int i = 0; i < this.mappings.size(); i++) {
			mapStrength = this.mappings.get(i);
			mapObj = new BasicDBObject();
			mapObj.put("sessionId", mapStrength.sessionId);
			mapObj.put("similarity", mapStrength.similarity);
			ls.add(mapObj);
		}
		
		return ls;
	}
	
	private class MapStrength implements Comparable<MapStrength> {
		
		public final int sessionId;
		public final double similarity;
		
		public MapStrength(int sessionId, double similarity) {
			this.sessionId = sessionId;
			this.similarity = similarity;
		}
		
		@Override
		public int compareTo(MapStrength other) {
			// sort in reverse order
			if(this.similarity < other.similarity) {
				return 1;
			} else if (this.similarity > other.similarity) {
				return -1;
			}
			return 0;
		}
		
	}
	
}
