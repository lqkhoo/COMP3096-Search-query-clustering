package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import writer.BatchFileWriter;
import writer.MongoWriter;

/**
 * Model for recording the strength of a mapping between a search session and a hierarchy class
 * @author Li Quan Khoo
 *
 */
@Deprecated
public class SessionToClassMappingOld {
	
	public static final String MODE_PARTIAL_MAPPINGS = "partial";
	public static final String MODE_FULL_MAPPINGS = "full";
	public static final String MODE_SESSION_IDS = "sessionids";
	
	private MongoWriter mongoWriter;
	private String mode;
	
	private HashMap<String, HashMap<Integer, Integer>> partial;	// This records all partial matches in session and how many matches
	private HashMap<String, HashSet<Integer>> full;				// This records all full string matches in session
	private HashMap<Integer, String[]> sessionIdMap;			// This records which searchStrings a sessionId maps to 
	
	private BatchFileWriter batchFileWriter;
	
	public SessionToClassMappingOld(MongoWriter mongoWriter, String mode) {
		this.mongoWriter = mongoWriter;
		this.mode = mode;
		this.partial = new HashMap<String, HashMap<Integer, Integer>>();
		this.full = new HashMap<String, HashSet<Integer>>();
		this.sessionIdMap = new HashMap<Integer, String[]>();
	}
	
	public void registerClass(String className) {
		if(! this.full.containsKey(className) && this.mode.equals(MODE_FULL_MAPPINGS)) {
			this.full.put(className, new HashSet<Integer>());
		}
		if(! this.partial.containsKey(className) && this.mode.equals(MODE_PARTIAL_MAPPINGS)) {
			this.partial.put(className, new HashMap<Integer, Integer>());
		}
	}
	
	public void strengthenRelation(String className, int sessionId, boolean isFullMatch) {
		registerClass(className);
		HashMap<Integer, Integer> innerPartialMap = this.partial.get(className);
		HashSet<Integer> innerFullMap = this.full.get(className);
		
		if(isFullMatch && this.mode.equals(MODE_FULL_MAPPINGS)) {
			innerFullMap.add(sessionId);
		} else {
			if(this.mode.equals(MODE_PARTIAL_MAPPINGS)){
				if(! innerPartialMap.containsKey(sessionId)) {
					innerPartialMap.put(sessionId, 1);
				} else {
					innerPartialMap.put(sessionId, innerPartialMap.get(sessionId) + 1);
				}
			}
		}
	}
	
	public void setSessionIdMapping(int sessionId, String[] searchStrings) {
		if(this.mode.equals(MODE_SESSION_IDS)) {
			this.sessionIdMap.put(sessionId, searchStrings);
		}
	}
	
	//TODO
	// write to file to see what it's like first
	public void toDB() {
		System.out.println("SessionToClassMapping: Writing to database");
		if(this.mode.equals(MODE_PARTIAL_MAPPINGS)) {
			System.out.println("NOT IMPLEMENTED");
			
		} else if(this.mode.equals(MODE_FULL_MAPPINGS)) {
			System.out.println("NOT IMPLEMENTED");
			
		} else if(this.mode.equals(MODE_SESSION_IDS)) {
			bestSessionIdMappingsToDb();
		}
	}
	
	/**
	 * This inserts all sessions which contain more than one match to a valid searchString
	 *   into the MongoDB collection "usefulSessions"
	 */
	private void bestSessionIdMappingsToDb() {
		
		String[] searchStrings;
		
		this.batchFileWriter = new BatchFileWriter("output/clusterer-out/sessions", "json");
		this.batchFileWriter.deleteFilesInDir();
		
		for(int sessionId : this.sessionIdMap.keySet().toArray(new Integer[]{})) {
			searchStrings = this.sessionIdMap.get(sessionId);
			
			// set to != 0 to include sessions with only one searchString match
			if(searchStrings.length > 1) {
				this.mongoWriter.addOrUpdateUsefulSession(sessionId, searchStrings);
			}
		}
	}
	
	/**
	 * Ouput to file
	 */
	public void toFile() {
		System.out.println("SessionToClassMapping: Writing to file");
		if(this.mode.equals(MODE_PARTIAL_MAPPINGS)) {
			partialMappingsToFile();
		} else if(this.mode.equals(MODE_FULL_MAPPINGS)) {
			fullMappingsToFile();
		} else if(this.mode.equals(MODE_SESSION_IDS)) {
			sessionIdMappingsToFile();
		}
	}
	
	private void partialMappingsToFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		int listLength = 0;
		ArrayList<SessionPartialMapping> mappings;
		
		this.batchFileWriter = new BatchFileWriter("output/clusterer-out/partial", "json");
		this.batchFileWriter.deleteFilesInDir();
		
		mappings = new ArrayList<SessionPartialMapping>();
		for(String className : this.partial.keySet().toArray(new String[]{})) {
			
			mappings.add(new SessionPartialMapping(className, this.partial.get(className)));
			listLength++;
			
			if(listLength % 10000 == 0) {
				this.batchFileWriter.writeToFile(gson.toJson(mappings), "partialMapping");
				mappings = new ArrayList<SessionPartialMapping>();
			}
		}
	}
	
	private void fullMappingsToFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		int listLength = 0;
		ArrayList<SessionFullMapping> mappings;
		
		this.batchFileWriter = new BatchFileWriter("output/clusterer-out/full", "json");
		this.batchFileWriter.deleteFilesInDir();
		
		mappings = new ArrayList<SessionFullMapping>();
		for(String className : this.full.keySet().toArray(new String[]{})) {
			
			mappings.add(new SessionFullMapping(className, this.full.get(className)));
			listLength++;
			
			if(listLength % 10000 == 0) {
				this.batchFileWriter.writeToFile(gson.toJson(mappings), "fullMapping");
				mappings = new ArrayList<SessionFullMapping>();
			}
		}
	}
	
	private void sessionIdMappingsToFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		int listLength = 0;
		ArrayList<SessionSearchStringMapping> mappings;
		String[] searchStrings;
		
		this.batchFileWriter = new BatchFileWriter("output/clusterer-out/sessions", "json");
		this.batchFileWriter.deleteFilesInDir();
		
		mappings = new ArrayList<SessionSearchStringMapping>();
		for(int sessionId : this.sessionIdMap.keySet().toArray(new Integer[]{})) {
			searchStrings = this.sessionIdMap.get(sessionId);
			
			if(searchStrings.length != 0) {
				mappings.add(new SessionSearchStringMapping(sessionId, this.sessionIdMap.get(sessionId)));
				listLength++;
				
				if(listLength % 1000000 == 0) {
					this.batchFileWriter.writeToFile(gson.toJson(mappings), "sessionMapping");
					mappings = new ArrayList<SessionSearchStringMapping>();
				}
			}
			// mappings.add(new SessionSearchStringMapping(className, this.sessionIdMap.get(className)));
		}
		
		if(mappings.size() != 0) {
			this.batchFileWriter.writeToFile(gson.toJson(mappings), "sessionMapping");
		}
	}
}
