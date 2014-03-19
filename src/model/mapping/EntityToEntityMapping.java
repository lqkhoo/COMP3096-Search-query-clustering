package model.mapping;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public class EntityToEntityMapping implements Comparable<EntityToEntityMapping> {
	
	public final int sessionId;
	public final String entityName;
	public final double mapStrength;
	public int frequency;
	
	public EntityToEntityMapping(int sessionId, String entityName, double mapStrength) {
		this.sessionId = sessionId;
		this.entityName = entityName;
		this.mapStrength = mapStrength;
		this.frequency = 0;
	}
	
	public int incrementFrequency() {
		this.frequency++;
		return this.frequency;
	}
	
	@Override
	public int compareTo(EntityToEntityMapping other) {
		// sort in reverse order
		if(this.mapStrength < other.mapStrength) {
			return 1;
		} else if(this.mapStrength > other.mapStrength) {
			return -1;
		}
		return 0;
	}

}
