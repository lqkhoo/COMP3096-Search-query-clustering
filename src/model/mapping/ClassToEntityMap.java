package model.mapping;

import writer.MongoWriter;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public class ClassToEntityMap extends AClusterMap {
	
	@Override
	public void toDB(MongoWriter mongoWriter) {
		String[] classNames = this.map.keySet().toArray(new String[]{});
		for(String className : classNames) {
			mongoWriter.setClassToEntityMapping(className, this.map.get(className), this.maxSessionsPerMapping);
		}
	}
	
}
