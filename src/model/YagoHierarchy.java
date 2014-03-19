package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import model.mapping.ClassToEntityMapping;

import writer.MongoWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Constructs a tree out of the given subset relations from YAGO
 * 
 * Turns out their data structure is not a tree... A child node can have up to six parents (the entity atropine)
 *   - that makes it a map, although most nodes (> 99.99%) have only one parent.
 * 
 * @author Li Quan Khoo
 */
public class YagoHierarchy {
	
	public static final String OUTPUT_PATH = "output/hierarchy-out/";
	
	// This is used during building only
	private HashMap<String, YagoClassNode> nodeMap;
	private MongoWriter mongoWriter;
	
	public YagoHierarchy(MongoWriter mongoWriter) {
		this.nodeMap = new HashMap<String, YagoClassNode>();
		this.mongoWriter = mongoWriter;
	}
	
	public void addClassToClassMapping(String subclassName, String superclassName) {
		
		YagoClassNode subclass;
		YagoClassNode superclass;
		
		if(! this.nodeMap.containsKey(subclassName)) {
			this.nodeMap.put(subclassName, new YagoClassNode(subclassName));
		}
		subclass = this.nodeMap.get(subclassName);
		
		if(! this.nodeMap.containsKey(superclassName)) {
			this.nodeMap.put(superclassName, new YagoClassNode(superclassName));
		}
		superclass = this.nodeMap.get(superclassName);
		
		subclass.addSuperclass(superclassName);
		superclass.addSubclass(subclassName);
		// this.nodeMap.put(subclassName, subclass);
		// this.nodeMap.put(superclassName, superclass);
	}
	
	public void addClassToEntityMapping(String className, int sessionId, String entityName, double mapStrength) {
		
		YagoClassNode cls;
		
		if(! this.nodeMap.containsKey(className)) {
			this.nodeMap.put(className, new YagoClassNode(className));
		}
		cls = this.nodeMap.get(className);
		
		cls.addEntityMapping(new ClassToEntityMapping(sessionId, entityName, mapStrength));
		
	}
	
	public void toFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String json = gson.toJson(this.nodeMap);
		
		File dir;
		File file;
		FileWriter fileWriter;
		BufferedWriter bufferedWriter = null;
		
		System.out.println("YagoHierarchy: Writing hierarchy to file");
		try {
			dir = new File(OUTPUT_PATH);
			dir.mkdirs();
			file = new File(OUTPUT_PATH + "hierarchy.json");
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(json);
			
		} catch (IOException e) {
			System.out.println("AYagoWriter: Error opening file " + OUTPUT_PATH + "hierarchy.json");
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {}
		}
	}
	
	public void toDBClassesCollection() {
		String[] nodeNames = this.nodeMap.keySet().toArray(new String[]{});
		String className;
		String[] superclassNames;
		String[] subclassNames;
		YagoClassNode node;
		
		System.out.println("YagoHierarchy: Writing hierarchy to MongoDb");
		for(String nodeName : nodeNames) {
			node = this.nodeMap.get(nodeName);
			className = node.getName();
			superclassNames = node.getSuperclassNames().toArray(new String[]{});
			subclassNames = node.getSubclassNames().toArray(new String[]{});
			this.mongoWriter.setClassHierarchy(className, superclassNames, subclassNames);
		}
	}
		
	public void fromDBClassesCollection() {
		DBCollection classes = this.mongoWriter.getClassesCollection();
		DBCursor cursor = classes.find(new BasicDBObject());
		DBObject cls;
		String name;
		String[] subclassNames;
		String[] superclassNames;
		
		YagoClassNode node;
		
		System.out.println("YagoHierarchy: Loading from MongoDB...");
		while(cursor.hasNext()) {
			cls = cursor.next();
			name = (String) cls.get("name");
			subclassNames = ((BasicDBList) cls.get("subclassNames")).toArray(new String[]{});
			superclassNames = ((BasicDBList) cls.get("superclassNames")).toArray(new String[]{});
			
			node = new YagoClassNode(name);
			node.setSubclasses(subclassNames);
			node.setSuperclasses(superclassNames);
			this.nodeMap.put(name, node);
		}
		
		System.out.println("YagoHierarchy: Finished loading from MongoDB.");
		/*
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		System.out.println(gson.toJson(this.nodeMap));
		*/
		
	}
		
}

