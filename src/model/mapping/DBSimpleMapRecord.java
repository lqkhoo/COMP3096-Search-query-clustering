package model.mapping;

import com.mongodb.BasicDBObject;

public class DBSimpleMapRecord implements Comparable<DBSimpleMapRecord> {
	
	private String name;
	private double mapStrength;
	
	public DBSimpleMapRecord(String name, double mapStrength) {
		this.name = name;
		this.mapStrength = mapStrength;
	}
	
	public DBSimpleMapRecord(String name, MapStrength mapStrength) {
		this.name = name;
		this.mapStrength = mapStrength.getMapStrength();
	}
	
	public BasicDBObject toDbObject() {
		BasicDBObject obj = new BasicDBObject();
		obj.put("name", this.name);
		obj.put("mapStrength", this.mapStrength);
		return obj;
	}
	
	@Override
	public int compareTo(DBSimpleMapRecord other) {
		// sort in reverse order
		if(this.mapStrength < other.mapStrength) {
			return 1;
		} else if(this.mapStrength > other.mapStrength) {
			return -1;
		}
		return 0;
	}
	
}
