package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Constructs a tree out of the given subset relations from YAGO
 * 
 * @author Li Quan Khoo
 */
@Deprecated
public class YagoHierarchyOld {
	
	public static final String OUTPUT_PATH = "output/hierarchy-out/";
	public static final String ROOT_NODE_NAME = "_ROOT_";
	
	// This is used during building only
	private HashMap<Integer, ArrayList<YagoClassNodeOld>> buildMap;
	private YagoClassNodeOld rootNode;
	private HashMap<String, YagoClassNodeOld> nodeMap;
	
	public YagoHierarchyOld() {
		this.buildMap = new HashMap<Integer, ArrayList<YagoClassNodeOld>>();
		this.rootNode = new YagoClassNodeOld(ROOT_NODE_NAME);
		this.nodeMap = new HashMap<String, YagoClassNodeOld>();
		this.nodeMap.put(ROOT_NODE_NAME, this.rootNode);
	}
	
	public void addRelation(String childNodeName, String ancestorNodeName) {
		
		YagoClassNodeOld child;
		YagoClassNodeOld ancestor;
		
		child = this.nodeMap.get(childNodeName);
		if(child == null) {
			child = new YagoClassNodeOld(childNodeName);
		}
		ancestor = this.nodeMap.get(ancestorNodeName);
		if(ancestor == null) {
			ancestor = new YagoClassNodeOld(ancestorNodeName);
		}
		child.addAncestor(ancestor);
		this.nodeMap.put(childNodeName, child);
	}
	
	public void buildHierarchy() {
		
		String[] keys;
		int	ancestorCount;
		YagoClassNodeOld node;
		ArrayList<YagoClassNodeOld> nodeArray;
		
		// order nodes by how many ancestors they have. Nodes with more ancestors MUST be at a lower level
		//   than those with less
		keys = this.nodeMap.keySet().toArray(new String[]{});
		for(int i = 0; i < keys.length; i++) {
			node = this.nodeMap.get(keys[i]);
			ancestorCount = node.getNumOfAncestors();
			nodeArray = this.buildMap.get(ancestorCount);
			if(nodeArray == null) {
				nodeArray = new ArrayList<YagoClassNodeOld>();
				this.buildMap.put(ancestorCount, nodeArray);
			}
			nodeArray.add(node);
		}
				
		Integer[] buildKeys;
		YagoClassNodeOld parent;
		YagoClassNodeOld ancestor;
		ArrayList<YagoClassNodeOld> ancestors;
		
		// Start from 0 and work our way up, establish the immediate parents. Those with 0 are top level classes
		//   and must be attached to root
		buildKeys = this.buildMap.keySet().toArray(new Integer[]{});
		
		for(int i = 0; i < buildKeys.length; i++) {
			nodeArray = this.buildMap.get(buildKeys[i]);
			System.out.println(nodeArray.size());
		}
		
		for(int i = 0; i < buildKeys.length; i++) {
			nodeArray = this.buildMap.get(buildKeys[i]);
			
			for(int j = 0; j < nodeArray.size(); j++) {
				node = nodeArray.get(j);
				ancestors = node.getAncestors();
				
				if(ancestors.size() == 0) {
					node.setParent(this.rootNode);
					this.rootNode.addChild(node);
				} else {
					parent = null;
					for(int k = 0; k < ancestors.size(); k++) {
						ancestor = ancestors.get(k);
						if(parent == null) {
							parent = ancestor;
						
						// find lowest-ranked ancestor - that's the immediate parent
						} else if (parent.getNumOfAncestors() > ancestor.getNumOfAncestors()){
							parent = ancestor;
						} // else do nothing
					}
					node.setParent(parent);
					parent.addChild(node);
				}
				
			}
			
		}
	}
	
	public void toFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		System.out.println(this.rootNode.getChildren());
		
		String json = gson.toJson(this.rootNode);
		
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

