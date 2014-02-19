package model;

import java.util.ArrayList;

public class YagoCategoryNodeOld {
	
	private String name;
	private String cleanName;
	private String categoryType;
	private String parentName;
	private transient YagoCategoryNodeOld parent;
	private ArrayList<String> ancestorNames;
	private transient ArrayList<YagoCategoryNodeOld> ancestors;
	private ArrayList<YagoCategoryNodeOld> children;
	
	public YagoCategoryNodeOld(String name) {
		this.name = name;
		this.cleanName = null;
		this.categoryType = null;
		this.parentName = null;
		this.parent = null;
		this.ancestorNames = new ArrayList<String>();
		this.ancestors = new ArrayList<YagoCategoryNodeOld>();
		this.children = new ArrayList<YagoCategoryNodeOld>();
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
	
	public YagoCategoryNodeOld getParent() {
		return this.parent;
	}
	
	public String getParentName() {
		return this.parentName;
	}
	
	public YagoCategoryNodeOld setParent(YagoCategoryNodeOld parent) {
		this.parent = parent;
		this.parentName = parent.getName();
		return parent;
	}
	
	public ArrayList<YagoCategoryNodeOld> getChildren() {
		return this.children;
	}
	
	public ArrayList<YagoCategoryNodeOld> getAncestors() {
		return this.ancestors;
	}
	
	public YagoCategoryNodeOld addAncestor(YagoCategoryNodeOld ancestor) {
		this.ancestors.add(ancestor);
		this.ancestorNames.add(ancestor.getName());
		return ancestor;
	}
	
	public int getNumOfAncestors() {
		return this.ancestors.size();
	}
	
	public ArrayList<String> getAncestorNames() {
		return this.ancestorNames;
	}
	
	public YagoCategoryNodeOld addChild(YagoCategoryNodeOld child) {
		this.children.add(child);
		return child;
	}
	
	public ArrayList<YagoCategoryNodeOld> addChildren(ArrayList<YagoCategoryNodeOld> children) {
		for(YagoCategoryNodeOld child : children) {
			this.children.add(child);
		}
		return children;
	}
	
	public YagoCategoryNodeOld removeChild(String nodeName) {
		for(int i = 0; i < this.children.size(); i++) {
			if(this.children.get(i).getName().equals(nodeName)) {
				return this.children.remove(i);
			}
		}
		return null;
	}
	
}
