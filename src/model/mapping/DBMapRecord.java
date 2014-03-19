package model.mapping;

import com.mongodb.BasicDBObject;

public class DBMapRecord implements Comparable<DBMapRecord> {
	
	private String name;
	private int sessionId;
	private double mapStrength;
	
	public DBMapRecord(String name, int sessionId, double mapStrength) {
		this.name = name;
		this.sessionId = sessionId;
		this.mapStrength = mapStrength;
	}
	
	public DBMapRecord(String name, MapStrength mapStrength) {
		this.name = name;
		this.sessionId = mapStrength.getSessionId();
		this.mapStrength = mapStrength.getMapStrength();
	}
	
	public BasicDBObject toDbObject() {
		BasicDBObject obj = new BasicDBObject();
		obj.put("name", this.name);
		obj.put("sessionId", this.sessionId);
		obj.put("mapStrength", this.mapStrength);
		return obj;
	}
	
	@Override
	public int compareTo(DBMapRecord other) {
		// sort in reverse order
		if(this.mapStrength < other.mapStrength) {
			return 1;
		} else if(this.mapStrength > other.mapStrength) {
			return -1;
		}
		return 0;
	}
	
}
