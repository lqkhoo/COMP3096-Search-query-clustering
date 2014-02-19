package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

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
	private HashMap<String, YagoCategoryNode> nodeMap;
	
	public YagoHierarchy() {
		this.nodeMap = new HashMap<String, YagoCategoryNode>();
	}
	
	public void addRelation(String subclassName, String superclassName) {
		
		YagoCategoryNode subclass;
		YagoCategoryNode superclass;
		
		subclass = this.nodeMap.get(subclassName);
		if(subclass == null) {
			subclass = new YagoCategoryNode(subclassName);
		}
		superclass = this.nodeMap.get(superclassName);
		if(superclass == null) {
			superclass = new YagoCategoryNode(superclassName);
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
	
}

