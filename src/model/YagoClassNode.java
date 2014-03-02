package model;

import java.util.ArrayList;

public class YagoClassNode {
	
	private String name;
	private String cleanName;
	private String categoryType;
	private ArrayList<String> superclasses;
	private ArrayList<String> subclasses;
	
	public YagoClassNode(String name) {
		this.name = name;
		this.cleanName = null;
		this.categoryType = null;
		this.superclasses = new ArrayList<String>();
		this.subclasses = new ArrayList<String>();
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
	
	public ArrayList<String> getSuperclassNames() {
		return this.superclasses;
	}
	
	public String addSubclass(String subclassName) {
		this.subclasses.add(subclassName);
		return subclassName;
	}
	
	public ArrayList<String> getSubclassNames() {
		return this.subclasses;
	}
	
}
