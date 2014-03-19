package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import model.mapping.ClassToEntityMapping;

/**
 * Object representing a Yago class within a YagoHierarchy instance
 * @author Li Quan Khoo
 */
public class YagoClassNode {
	
	private String name;
	private String cleanName;
	private String categoryType;
	private ArrayList<String> superclasses;
	private ArrayList<String> subclasses;
	private ArrayList<ClassToEntityMapping> entityMappings;
	
	private int entityMapCountLimit = 100;
	
	public YagoClassNode(String name) {
		this.name = name;
		this.cleanName = null;
		this.categoryType = null;
		this.superclasses = new ArrayList<String>();
		this.subclasses = new ArrayList<String>();
		this.entityMappings = new ArrayList<ClassToEntityMapping>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getCleanName() {
		return this.cleanName;
	}
	
	public String getCategoryType() {
		return this.categoryType;
	}
	
	public String addSuperclass(String superclassName) {
		this.superclasses.add(superclassName);
		return superclassName;
	}
	
	public ArrayList<String> setSuperclasses(String[] superclasses) {
		this.superclasses.addAll(Arrays.asList(superclasses));
		return this.superclasses;
	}
	
	public ArrayList<String> getSuperclassNames() {
		return this.superclasses;
	}
	
	public String addSubclass(String subclassName) {
		this.subclasses.add(subclassName);
		return subclassName;
	}
	
	public ArrayList<String> setSubclasses(String[] subclasses) {
		this.subclasses.addAll(Arrays.asList(subclasses));
		return this.subclasses;
	}
	
	public ArrayList<String> getSubclassNames() {
		return this.subclasses;
	}
	
	public int setEntityMapCountLimit(int entityMapCountLimit) {
		this.entityMapCountLimit = entityMapCountLimit; 
		return this.entityMapCountLimit;
	}
	
	public ArrayList<ClassToEntityMapping> addEntityMapping(ClassToEntityMapping mapping) {
		this.entityMappings.add(mapping);
		return this.entityMappings;
	}
	
	public void sortEntityMappings() {
		Collections.sort(this.entityMappings);
	}
	
	public ArrayList<ClassToEntityMapping> getEntityMappings() {
		return this.entityMappings;
	}
	
	public BasicDBList mappingsAsBasicDBList() {
		ClassToEntityMapping mapping;
		BasicDBList ls = new BasicDBList();
		for(int i = 0; i < Math.min(this.entityMappings.size(), this.entityMapCountLimit); i++) {
			mapping = this.entityMappings.get(i);
			ls.add(mapping.asBasicDBObject());
		}
		
		return ls;
	}
	
}
