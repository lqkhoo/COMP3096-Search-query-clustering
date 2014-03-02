package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import writer.MongoWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	public void addRelation(String subclassName, String superclassName) {
		
		YagoClassNode subclass;
		YagoClassNode superclass;
		
		subclass = this.nodeMap.get(subclassName);
		if(subclass == null) {
			subclass = new YagoClassNode(subclassName);
		}
		superclass = this.nodeMap.get(superclassName);
		if(superclass == null) {
			superclass = new YagoClassNode(superclassName);
		}
		subclass.addSuperclass(superclassName);
		superclass.addSubclass(subclassName);
		this.nodeMap.put(subclassName, subclass);
		this.nodeMap.put(superclassName, superclass);
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
	
	public void toDb() {
		String[] nodeNames = this.nodeMap.keySet().toArray(new String[]{});
		String className;
		String[] superclassNames;
		String[] subclassNames;
		
		System.out.println("YagoHierarchy: Writing hierarchy to MongoDb");
		for(String nodeName : nodeNames) {
			YagoClassNode node = this.nodeMap.get(nodeName);
			className = node.getName();
			superclassNames = node.getSuperclassNames().toArray(new String[]{});
			subclassNames = node.getSubclassNames().toArray(new String[]{});
			this.mongoWriter.setClassHierarchy(className, superclassNames, subclassNames);
		}
	}
	
}

