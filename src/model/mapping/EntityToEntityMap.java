package model.mapping;

import writer.MongoWriter;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public class EntityToEntityMap extends AClusterMap {
	
	@Override
	public void toDB(MongoWriter mongoWriter) {
		String[] entityNames = this.map.keySet().toArray(new String[]{});
		for(String entityName : entityNames) {
			mongoWriter.setEntityToEntityMapping(entityName, this.map.get(entityName), this.maxSessionsPerMapping);
		}
	}
	
}
