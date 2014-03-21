package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lib.Stemmer;
import model.SearchSessionSerial;
import model.SemanticSession;
import model.mapping.ClassToEntityMap;
import model.mapping.ClassToStringMap;
import model.mapping.EntityToClassMapping;
import model.mapping.EntityToEntityMap;
import model.mapping.SessionSearchStringMapping;

import reader.DBCacheReader;
import reader.PreprocessedLogReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import writer.BatchFileWriter;
import writer.MongoWriter;

/**
 * This class produces two MongoDB collections which make up the session clusters:
 *   "sessionClusters", and "augmentedSessions". Currently it runs using data already
 *   output by QueryMapper to save time, but it is possible to get the same output from
 *   AOL log files and just YAGO entities and classes
 * 
 * @author Li Quan Khoo
 *
 */
public class SessionClusterer {
	
	public static final String DEFAULT_STOPWORDS_INPUT_FILE_PATH = "src/config/stopwords.ini";
	
	private PreprocessedLogReader logReader;
	private MongoWriter mongoWriter;
	private BatchFileWriter batchFileWriter;
	
	private Stemmer stemmer;
	private HashSet<String> stopwords;
	
	// Maps searchString to a list of their class nIds
	private HashMap<String, int[]> searchStringMap = new HashMap<String, int[]>();
	
	// Maps classes with sessionIds, taking into account mapping strength
	// private SessionToClassMapping sessionMap;
	private HashMap<Integer, String[]> sessionMap;
	
	long prevReportTime = System.currentTimeMillis();
	
	public SessionClusterer(MongoWriter mongoWriter) {
		this.logReader = new PreprocessedLogReader();
		this.mongoWriter = mongoWriter;
		this.stemmer = new Stemmer();
		this.sessionMap = new HashMap<Integer, String[]>();
		initStopwords();
	}
	
	private void initStopwords() {
		this.stopwords = new HashSet<String>();
		
		File inputFile = new File(DEFAULT_STOPWORDS_INPUT_FILE_PATH);
		try {
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = br.readLine();
			String word = null;
			while(line != null) {
				word = line.replaceAll("[\n\r]", "");
				stopwords.add(word);
				line = br.readLine();
			}
			br.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("QueryMapper: Stopwords file not found");
		} catch (IOException e) {
			System.out.println("QueryMapper: IO exception reading stopwords file");
		}
	}
	
	/**
	 * First runnable method. This identifies sessions which contain more than one valid
	 *   searchString and puts them into MongoDB.
	 * 
	 * Loads relevant MongoDB data into RAM for performance reasons and then processes sessions.
	 */
	public void findUsefulSessions() {
		loadSearchStringsToClassMappingsFromFile();
		processSessions();
		// usefulSessionsToFile();	// uncomment this line if you want file output alongside db output
		usefulSessionsToDB();
	}
	
	/**
	 * Second runnable method. This augments sessions with the entities which are most likely being searched for etc.
	 * 
	 * Runtime: ~ minutes to ~ 2 hours depending on similarityThreshold. The bigger the faster.
	 */
	public void deriveSessionSemantics(double similarityThreshold) {
		// Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		DBCollection usefulSessions = this.mongoWriter.getUsefulSessionsCollection();
		DBCursor cursor;
		DBObject session;
		int sessionId;
		BasicDBList searchStrings;
		SemanticSession semanticSession;
		
		int sessionsProcessed = 0;
		
		// Test session with 4 searchStrings
		// cursor = usefulSessions.find(new BasicDBObject("sessionId", 3925986));
		
		cursor = usefulSessions.find(new BasicDBObject());
		while(cursor.hasNext()) {
			session = cursor.next();
			sessionId = (Integer) session.get("sessionId");
			searchStrings = (BasicDBList) session.get("searchStrings");
			
			semanticSession = new SemanticSession(sessionId, searchStrings.toArray(new String[]{}), similarityThreshold, this.mongoWriter);
			
			// System.out.println(gson.toJson(semanticSession));
			
			this.mongoWriter.addSemanticSession(semanticSession, similarityThreshold);
			
			// System.out.println(gson.toJson(this.mongoWriter.getSemanticSessionsCollection().findOne(new BasicDBObject("sessionId", sessionId))));
			
			// testing only -- process one usefulSession
			// break;
			reportSessionsProcessed(++sessionsProcessed);
		}
	}
	
	/**
	 * Third runnable method. This classifies the semantic sessions under yago classes
	 */
	public void clusterSessions() {
		// Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		// Hash of entity names against their mappings to semantic sessions (their sessionIds)
		HashMap<String, EntityToClassMapping> map = new HashMap<String, EntityToClassMapping>();
		
		DBCollection semanticSessions = this.mongoWriter.getSemanticSessionsCollection();
		
		DBCursor cursor;
		DBObject semanticSession;
		BasicDBList similarities;
		BasicDBObject similarityObj;
		int sessionId;
		String entity1Name;
		String entity1SearchString;
		String entity2Name;
		String entity2SearchString;
		double similarity;
		
		EntityToClassMapping mapping;
		
		int sessionsProcessed = 0;
		
		System.out.println("SessionClusterer: Clustering sessions.");
		cursor = semanticSessions.find(new BasicDBObject());
		while(cursor.hasNext()) {
			semanticSession = cursor.next();
			similarities = (BasicDBList) semanticSession.get("similarities");
			sessionId = (Integer) semanticSession.get("sessionId");
			if(similarities != null) {
				for(int i = 0; i < similarities.size(); i++) {
					similarityObj = (BasicDBObject) similarities.get(i);
					if(similarityObj != null) {
						
						similarity = (Double) similarityObj.get("similarity");
						entity1Name = (String) similarityObj.get("entity1");
						entity1SearchString = (String) similarityObj.get("entity1SearchString");
						entity2Name = (String) similarityObj.get("entity2");
						entity2SearchString = (String) similarityObj.get("entity2SearchString");
						
						if(! map.containsKey(entity1Name)) {
							map.put(entity1Name, new EntityToClassMapping(entity1Name, entity1SearchString));
						}
						if(! map.containsKey(entity2Name)) {
							map.put(entity2Name, new EntityToClassMapping(entity2Name, entity2SearchString));
						}
						
						map.get(entity1Name).addMapping(sessionId, similarity);
						
					}
				}
			}
			
			reportSessionsProcessed(++sessionsProcessed);
			// System.out.println(gson.toJson(semanticSession));
			
		}
		
		sessionsProcessed = 0;
		System.out.println("SessionClusterer: Writing to database.");
		// write to db
		for(String key : map.keySet()) {
			mapping = map.get(key);
			mapping.sortMappings();	// Sort mappings before insertion
			this.mongoWriter.addSessionCluster(mapping);
			
			reportSessionsProcessed(++sessionsProcessed);
		}
	}
	
	/**
	 * Fourth runnable method. Gives mappings from a Yago class to its Entities based on 
	 *   how likely it is for a searchString to mean that Entity within a given session (sessionId)
	 */
	public void constructClassToEntityMappings() {
		
		DBCollection semanticSessions = this.mongoWriter.getSemanticSessionsCollection();
		DBCursor cursor;
		DBObject semanticSession;
		BasicDBList similarities;
		int sessionId;
		
		BasicDBObject similarity;
		double similarityScore;
		String entity1Name;
		String entity2Name;
		BasicDBList commonClasses;
		
		String commonClass;
		
		int sessionsProcessed = 0;
		
		ClassToEntityMap map = new ClassToEntityMap();
		
		cursor = semanticSessions.find(new BasicDBObject());
		while(cursor.hasNext()) {
			semanticSession = cursor.next();
			sessionId = (Integer) semanticSession.get("sessionId");
			similarities = (BasicDBList) semanticSession.get("similarities");
			if(similarities != null) {
				for(int i = 0; i < similarities.size(); i++) {
					similarity = (BasicDBObject) similarities.get(i);
					similarityScore = (Double) similarity.get("similarity");
					entity1Name = (String) similarity.get("entity1");
					entity2Name = (String) similarity.get("entity2");
					commonClasses = (BasicDBList) similarity.get("commonClasses");
					// ignore common links
					
					if(commonClasses != null) {
						for(int j = 0; j < commonClasses.size(); j++) {
							commonClass = (String) commonClasses.get(j);
							
							map.addMapping(commonClass, entity1Name, sessionId, similarityScore);
							map.addMapping(commonClass, entity2Name, sessionId, similarityScore);
						}
					}
				}
			}			
			reportSessionsProcessed(++sessionsProcessed);
			
		}
		System.out.println(map.getMap().keySet().size());
		
		map.toDB(this.mongoWriter);
	}
	
	/**
	 * Fifth runnable method. Gives mappings from a Yago Entity to another Entity based on 
	 *   how likely it is for a searchString to mean those Entities within a given session (sessionId)
	 */
	public void constructEntityToEntityMappings() {
		
		DBCollection semanticSessions = this.mongoWriter.getSemanticSessionsCollection();
		DBCursor cursor;
		DBObject semanticSession;
		BasicDBList similarities;
		int sessionId;
		
		BasicDBObject similarity;
		double similarityScore;
		String entity1Name;
		String entity2Name;
		BasicDBList commonClasses;
		
		String commonClass;
		
		int sessionsProcessed = 0;
		
		EntityToEntityMap map = new EntityToEntityMap();
		
		cursor = semanticSessions.find(new BasicDBObject());
		while(cursor.hasNext()) {
			semanticSession = cursor.next();
			sessionId = (Integer) semanticSession.get("sessionId");
			similarities = (BasicDBList) semanticSession.get("similarities");
			if(similarities != null) {
				for(int i = 0; i < similarities.size(); i++) {
					similarity = (BasicDBObject) similarities.get(i);
					similarityScore = (Double) similarity.get("similarity");
					entity1Name = (String) similarity.get("entity1");
					entity2Name = (String) similarity.get("entity2");
					commonClasses = (BasicDBList) similarity.get("commonClasses");
					// ignore common links
					
					if(commonClasses != null) {
						for(int j = 0; j < commonClasses.size(); j++) {
							commonClass = (String) commonClasses.get(j);
							
							map.addMapping(entity1Name, entity2Name, sessionId, similarityScore);
							map.addMapping(entity2Name, entity1Name, sessionId, similarityScore);
						}
					}
				}
			}
			reportSessionsProcessed(++sessionsProcessed);
		}
		
		map.toDB(this.mongoWriter);
	}
	
	/**
	 * Sixth runnable method. Gives mappings from a Yago class to strings within AOL log sessions
	 */
	public void constructClassToStringMappings() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		DBCollection semanticSessions = this.mongoWriter.getSemanticSessionsCollection();
		DBCursor cursor;
		DBObject semanticSession;
		BasicDBList similarities;
		BasicDBList queryStrings;
		int sessionId;
		
		BasicDBObject similarity;
		double similarityScore;
		String entity1Name;
		String entity1SearchString;
		String entity2Name;
		String entity2SearchString;
		BasicDBList commonClasses;
		
		String commonClass;
		
		int sessionsProcessed = 0;
		
		HashSet<String> processedEntities;
		ClassToStringMap map = new ClassToStringMap();
		
		cursor = semanticSessions.find(new BasicDBObject());
		while(cursor.hasNext()) {
			semanticSession = cursor.next();
			
			processedEntities = new HashSet<String>();
			
			sessionId = (Integer) semanticSession.get("sessionId");
			similarities = (BasicDBList) semanticSession.get("similarities");
			queryStrings = (BasicDBList) semanticSession.get("queryStrings");
			if(similarities != null) {
				for(int i = 0; i < similarities.size(); i++) {
					similarity = (BasicDBObject) similarities.get(i);
					similarityScore = (Double) similarity.get("similarity");
					entity1Name = (String) similarity.get("entity1");
					entity1SearchString = (String) similarity.get("entity1SearchString");
					entity2Name = (String) similarity.get("entity2");
					entity2SearchString = (String) similarity.get("entity2SearchString");
					commonClasses = (BasicDBList) similarity.get("commonClasses");
					// ignore common links
					
					getStringToClassMapping(entity1Name, entity1SearchString, queryStrings, commonClasses, processedEntities, map);
					getStringToClassMapping(entity2Name, entity2SearchString, queryStrings, commonClasses, processedEntities, map);
					
				}
			}
			reportSessionsProcessed(++sessionsProcessed);
		}
		// System.out.println(gson.toJson(map));
		// System.out.println(map.getMap().keySet().size());
		map.toDB(this.mongoWriter);
	}
	
	/**
	 * Helper for the above function
	 */
	private void getStringToClassMapping(String entityName, String entitySearchString, BasicDBList queryStrings,
			BasicDBList commonClasses, HashSet<String> processedEntities, ClassToStringMap map) {
		
		// If entity has already been accounted for then do nothing
		if(processedEntities.contains(entityName)) {
			return;
		}
		
		if(queryStrings == null) {
			return;
		}
		
		for(int i = 0; i < queryStrings.size(); i++) {
			
			Pattern linePattern = Pattern.compile("((.*)[\\s]?(" + entitySearchString + ")[\\s]?(.*)?)");
			Matcher lineMatcher;
			
			String before;
			String searchString;
			String after;
			
			lineMatcher = linePattern.matcher((String) queryStrings.get(i));
			if(lineMatcher.find()) {
				
				/*
				System.out.println(lineMatcher.group(1));	// the whole line
				System.out.println(lineMatcher.group(2));	// before
				System.out.println(lineMatcher.group(3));	// searchString
				System.out.println(lineMatcher.group(4));	// after 
				*/
				
				before = lineMatcher.group(2);
				searchString = lineMatcher.group(3);
				after = lineMatcher.group(4);
				
				for(int j = 0; j < commonClasses.size(); j++) {
					if(! before.equals("") && before.split("\\s+").length < 3) {
						map.addMapping((String) commonClasses.get(j), before.trim(), 0, 1);
					}
					if(! after.equals("") && after.split("\\s+").length < 3) {
						map.addMapping((String) commonClasses.get(j), after.trim(), 0, 1);
					}
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * Loads the entitiesToClassIds output from DBCacher
	 */
	private void loadSearchStringsToClassMappingsFromFile() {
		DBCacheReader cacheReader = new DBCacheReader();
		this.searchStringMap = cacheReader.readSearchStringToClassMappings();
	}
	
	/**
	 * Process sessions into data structures which are then inserted into MongoDB
	 * This initializes sessionMap
	 */
	private void processSessions() {
		
		int sessionId;
		ArrayList<String> substrings;	// substrings formed from whole query string
		String stemmedSearchString;
		String substring;
		HashMap<String, Boolean> processedQueriesInSession;
		HashMap<String, Boolean> validSearchStrings;
		
		int sessionsProcessed = 0;
		
		SearchSessionSerial[] sessions = logReader.getLogs();
		while(sessions != null) {
			for(SearchSessionSerial session : sessions) {
				
				sessionId = session.getSessionId();
				processedQueriesInSession = new HashMap<String, Boolean>();
				
				// for each query in session
				for(String query : session.getQueries()) {
					
					// Only process each identical query once in each session
					if(processedQueriesInSession.containsKey(query)) { continue; }
					else { processedQueriesInSession.put(query, true); }
					
					substrings = generateQuerySubstrings(query);
					validSearchStrings = new HashMap<String, Boolean>();
					// Algorithm assumes substrings are passed down from longest to shortest
					
					// for each query substring
					for(int i = 0; i < substrings.size(); i++) {
						
						substring = substrings.get(i);
						if(! this.stopwords.contains(substring)
								&& this.searchStringMap.containsKey(substring)) {
							validSearchStrings.put(substring, true);
							removeSubstrings(substrings, substring);
							
						} else {
							stemmedSearchString = stemQueryString(substring);
							if(! this.stopwords.contains(stemmedSearchString)
									&& this.searchStringMap.containsKey(stemmedSearchString)) {
								validSearchStrings.put(stemmedSearchString, true);
								removeSubstrings(substrings, substring); // remove the originals, not the stemmed ones
							}
						} // else do nothing
					}
					
					this.sessionMap.put(sessionId, validSearchStrings.keySet().toArray(new String[]{}));
					
				}
				reportSessionsProcessed(++sessionsProcessed);
			}
			/* test stuff with just one file ---
			if(sessionsProcessed >= 100000) {
				break;
			}
			*/
			
			sessions = logReader.getLogs();
		}
		
	}
	
	private void usefulSessionsToDB() {
		String[] searchStrings;
		
		int sessionsWritten = 0;
		// WARNING: drops previous data before running in case sessionIds get adjusted to avoid duplicates
		this.mongoWriter.getUsefulSessionsCollection().drop();
		
		for(int sessionId : this.sessionMap.keySet().toArray(new Integer[]{})) {
			searchStrings = this.sessionMap.get(sessionId);
			
			// set to != 0 to include sessions with only one searchString match
			
			if(searchStrings.length > 1) {
				this.mongoWriter.addOrUpdateUsefulSession(sessionId, searchStrings);
				reportSessionsWrittenToDb(++sessionsWritten);
			}
			
		}
	}
	
	private void usefulSessionsToFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		int listLength = 0;
		ArrayList<SessionSearchStringMapping> mappings;
		String[] searchStrings;
		
		this.batchFileWriter = new BatchFileWriter("output/clusterer-out/sessions", "json");
		this.batchFileWriter.deleteFilesInDir();
		
		mappings = new ArrayList<SessionSearchStringMapping>();
		for(int sessionId : this.sessionMap.keySet().toArray(new Integer[]{})) {
			searchStrings = this.sessionMap.get(sessionId);
			
			if(searchStrings.length > 1) {
				mappings.add(new SessionSearchStringMapping(sessionId, this.sessionMap.get(sessionId)));
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
		
	/**
	 * From a given string, generate all possible substrings (keeping word order) with space-delimited words
	 * @param query
	 * @return
	 */
	private ArrayList<String> generateQuerySubstrings(String query) {
		
		ArrayList<String> substrings = new ArrayList<String>();
		String[] parts = query.split(" ");
		String substring;
		
		// degenerate
		if(query.equals("") || query == null) {
			return substrings;
		}
		
		//          0     1     2     3
		// String [***] [***] [***] [***]
		//          ^                 ^
		//          i                 j
		//
		
		for(int j = parts.length - 1; j >= 0; j--) {
			
			for(int i = 0; i + j < parts.length; i++) {
				substring = "";
				for(int k = i; k <= i + j; k++) {
					if(substring.equals("")) {
						substring += parts[k];
					} else {
						substring += " " + parts[k];
					}
				}
				substrings.add(substring);
			}
		}
		
		return substrings;
		
	}
	
	/**
	 * Given an arraylist and a string, this removes all instances of substrings of that string (space-delimited) from the arraylist
	 */
	private void removeSubstrings(ArrayList<String> mainArray, String str) {
		ArrayList<String> substrings = generateQuerySubstrings(str);
		
		for(int i = 0; i < substrings.size(); i++) {
			if(mainArray.contains(substrings.get(i))) {
				mainArray.remove(substrings.get(i));
			}
		}
	}

	/**
	 * This runs Stemmer against the given input and returns the output
	 * @param queryString
	 * @return
	 */
	private String stemQueryString(String queryString) {
		String output = "";
		char[] charArray;
		
		String[] tokens = queryString.split(" ");
		for(String token : tokens) {
			if(! this.stopwords.contains(token)) {
				
				this.stemmer = new Stemmer();
				charArray = token.toCharArray();
				this.stemmer.add(charArray, charArray.length);
				this.stemmer.stem();
				if(output.equals("")) {
					output += this.stemmer.toString();
				} else {
					output += " " + this.stemmer.toString();
				}
			}
		}
		return output;
	}	
	
	private void reportSessionsProcessed(int sessionsProcessed) {
		if(sessionsProcessed % 1000 == 0) {
			long duration = (System.currentTimeMillis() - prevReportTime) / 1000;
			prevReportTime = System.currentTimeMillis();
			System.out.println("SessionClusterer: Sessions processed: " + sessionsProcessed / 1000 + "k entities. (" + duration + " s)");
		}
	}
	
	private void reportSessionsWrittenToDb(int sessionsWritten) {
		if(sessionsWritten % 10000 == 0) {
			long duration = (System.currentTimeMillis() - prevReportTime) / 1000;
			prevReportTime = System.currentTimeMillis();
			System.out.println("SessionClusterer: Sessions written: " + sessionsWritten / 1000 + "k entities. (" + duration + " s)");
		}
	}
	
}
