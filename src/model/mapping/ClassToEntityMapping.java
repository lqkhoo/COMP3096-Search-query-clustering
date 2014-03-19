package model.mapping;

import com.mongodb.BasicDBObject;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public class ClassToEntityMapping implements Comparable<ClassToEntityMapping> {
	
	public final int sessionId;
	public final String entityName;
	public final double mapStrength;
	
	public ClassToEntityMapping(int sessionId, String entityName, double mapStrength) {
		this.sessionId = sessionId;
		this.entityName = entityName;
		this.mapStrength = mapStrength;
	}
	
	@Override
	public int compareTo(ClassToEntityMapping other) {
		// sort in reverse order
		if(this.mapStrength < other.mapStrength) {
			return 1;
		} else if(this.mapStrength > other.mapStrength) {
			return -1;
		}
		return 0;
	}
	
	public BasicDBObject asBasicDBObject() {
		BasicDBObject obj = new BasicDBObject();
		obj.put("sessionId", this.sessionId);
		obj.put("name", this.entityName);
		obj.put("mapStrength", this.mapStrength);
		return obj;
	}
}
