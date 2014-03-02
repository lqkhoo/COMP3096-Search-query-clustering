package model;

import java.util.ArrayList;

@Deprecated
public class YagoClassNodeOld {
	
	private String name;
	private String cleanName;
	private String categoryType;
	private String parentName;
	private transient YagoClassNodeOld parent;
	private ArrayList<String> ancestorNames;
	private transient ArrayList<YagoClassNodeOld> ancestors;
	private ArrayList<YagoClassNodeOld> children;
	
	public YagoClassNodeOld(String name) {
		this.name = name;
		this.cleanName = null;
		this.categoryType = null;
		this.parentName = null;
		this.parent = null;
		this.ancestorNames = new ArrayList<String>();
		this.ancestors = new ArrayList<YagoClassNodeOld>();
		this.children = new ArrayList<YagoClassNodeOld>();
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
	
	public YagoClassNodeOld getParent() {
		return this.parent;
	}
	
	public String getParentName() {
		return this.parentName;
	}
	
	public YagoClassNodeOld setParent(YagoClassNodeOld parent) {
		this.parent = parent;
		this.parentName = parent.getName();
		return parent;
	}
	
	public ArrayList<YagoClassNodeOld> getChildren() {
		return this.children;
	}
	
	public ArrayList<YagoClassNodeOld> getAncestors() {
		return this.ancestors;
	}
	
	public YagoClassNodeOld addAncestor(YagoClassNodeOld ancestor) {
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
	
	public YagoClassNodeOld addChild(YagoClassNodeOld child) {
		this.children.add(child);
		return child;
	}
	
	public ArrayList<YagoClassNodeOld> addChildren(ArrayList<YagoClassNodeOld> children) {
		for(YagoClassNodeOld child : children) {
			this.children.add(child);
		}
		return children;
	}
	
	public YagoClassNodeOld removeChild(String nodeName) {
		for(int i = 0; i < this.children.size(); i++) {
			if(this.children.get(i).getName().equals(nodeName)) {
				return this.children.remove(i);
			}
		}
		return null;
	}
	
}
