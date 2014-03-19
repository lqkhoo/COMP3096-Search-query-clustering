package model.mapping;

import com.mongodb.BasicDBObject;

public class MapStrength implements Comparable<MapStrength> {
	
	private int sessionId;
	private double mapStrength;
	
	public MapStrength(int sessionId, double mapStrength) {
		this.sessionId = sessionId;
		this.mapStrength = mapStrength;
	}
	
	public BasicDBObject asDBObject() {
		BasicDBObject obj = new BasicDBObject();
		obj.put("sessionId", this.sessionId);
		obj.put("mapStrength", this.mapStrength);
		return obj;
	}
	
	public int getSessionId() {
		return this.sessionId;
	}
	
	public double getMapStrength() {
		return this.mapStrength;
	}
	
	@Override
	public int compareTo(MapStrength other) {
		// sort in reverse order
		if(this.mapStrength < other.mapStrength) {
			return 1;
		} else if(this.mapStrength > other.mapStrength) {
			return -1;
		}
		return 0;
	}
	
}
