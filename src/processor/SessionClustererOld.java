package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lib.Stemmer;
import model.SearchSessionSerial;
import model.SessionToClassMapping;

import reader.DBCacheReader;
import reader.PreprocessedLogReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

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
@Deprecated
public class SessionClustererOld {
	
	public static final String DEFAULT_STOPWORDS_INPUT_FILE_PATH = "src/config/stopwords.ini";
	
	private PreprocessedLogReader logReader;
	private MongoWriter mongoWriter;
	private Stemmer stemmer;
	private HashMap<String, String> stopwords;
	
	// Maps searchString to a list of their class nIds
	private HashMap<String, int[]> searchStringMap = new HashMap<String, int[]>();
	
	// Maps entity names to names of their classes
	private HashMap<String, String[]> entityClassMap = new HashMap<String, String[]>(); 
	
	// Maps classes with sessionIds, taking into account mapping strength
	private SessionToClassMapping sessionMap;
	
	long prevReportTime = System.currentTimeMillis();
	
	public SessionClustererOld(MongoWriter mongoWriter, String sessionMapMode) {
		this.logReader = new PreprocessedLogReader();
		this.mongoWriter = mongoWriter;
		this.stemmer = new Stemmer();
		this.sessionMap = new SessionToClassMapping(this.mongoWriter, sessionMapMode);
		initStopwords();
	}
	
	private void initStopwords() {
		this.stopwords = new HashMap<String, String>();
		
		File inputFile = new File(DEFAULT_STOPWORDS_INPUT_FILE_PATH);
		try {
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = br.readLine();
			String word = null;
			while(line != null) {
				word = line.replaceAll("[\n\r]", "");
				stopwords.put(word, "");
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
	 * Loads relevant MongoDB data into RAM for performance reasons and then processes sessions.
	 * Finally, it records the new data into MongoDB
	 */
	public void run() {
		loadEntitiesFromFile();
		// loadClasses();
		processSessions();
		toDB();
	}
	
	/**
	 * Load classes into memory. This initializes entityClassMap
	 */
	private void loadClasses() {
		
		DBCollection classes;
		DBObject cls;
		DBCursor cursor;
		String className;
		
		int classesProcessed = 0;
		
		classes = this.mongoWriter.getClassesCollection();
		cursor = classes.find(new BasicDBObject());
		
		while(cursor.hasNext()) {
			cls = cursor.next();
			className = (String) cls.get("name");
			sessionMap.registerClass(className);
			
			reportClassesProcessed(++classesProcessed);
		}
	}
	
	/**
	 * 
	 */
	private void loadEntitiesFromFile() {
		DBCacheReader cacheReader = new DBCacheReader();
		this.searchStringMap = cacheReader.readSearchStringToClassMappings();
	}
	
	/**
	 * Process sessions into data structures which are then inserted into MongoDB
	 * This initializes sessionMap
	 */
	private void processSessions() {
		
		DBCollection entities = this.mongoWriter.getEntitiesCollection();
		int sessionId;
		ArrayList<String> substrings;	// substrings formed from whole query string
		String stemmedSearchString;
		String substring;
		HashMap<String, Boolean> processedQueriesInSession;
		HashMap<String, Boolean> validSearchStrings;
		boolean isFullMatch;
		
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
						isFullMatch = (i == 0) ? true : false;
						
						substring = substrings.get(i);
						//if(entities.find(new BasicDBObject("searchString", substring)).hasNext()) {
						if(this.searchStringMap.containsKey(substring)) {
							validSearchStrings.put(substring, true);
							updateSessionMap(substring, isFullMatch, sessionId);
							removeSubstrings(substrings, substring);
							
						} else {
							stemmedSearchString = stemQueryString(substring);
							//if(entities.find(new BasicDBObject("searchString", stemmedSearchString)).hasNext()) {
							if(this.searchStringMap.containsKey(stemmedSearchString)) {
								validSearchStrings.put(stemmedSearchString, true);
								updateSessionMap(stemmedSearchString, isFullMatch, sessionId);
								removeSubstrings(substrings, substring); // remove the originals, not the stemmed ones
							}
						} // else do nothing
					}
					
					this.sessionMap.setSessionIdMapping(sessionId, validSearchStrings.keySet().toArray(new String[]{}));
					
				}
				reportSessionsProcessed(++sessionsProcessed);
			}
			/*	test stuff
			if(sessionsProcessed >= 100000) {
				break;
			}
			*/
			sessions = logReader.getLogs();
		}
		
	}
	
	private void toDB() {
		this.sessionMap.toDB();
	}
	
	private void toFile() {
		this.sessionMap.toFile();
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
			new ArrayList<String>();
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
			if(! this.stopwords.containsKey(token)) {
				
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
	
	/**
	 * Updates sessionMap with appropriate data given a substring of the query string and the sessionId in question
	 * @param substring
	 * @param sessionId
	 */
	private void updateSessionMap(String substring, boolean isFullMatch, int sessionId) {
		DBObject entity;
		BasicDBObject entityRelations;
		BasicDBList entityClasses;
		String entityClassName;
		
		
		DBCollection entities = this.mongoWriter.getEntitiesCollection();
		DBCursor cursor;
		
		cursor = entities.find(new BasicDBObject("searchString", substring));
		while(cursor.hasNext()) {
			entity = cursor.next();
			entityRelations = (BasicDBObject) entity.get("relations");
			if(entityRelations != null) {
				entityClasses = (BasicDBList) entityRelations.get("rdf:type");
				if(entityClasses != null) {
					for(int j = 0; j < entityClasses.size(); j++) {
						entityClassName = (String) entityClasses.get(j);
						sessionMap.strengthenRelation(entityClassName, sessionId, isFullMatch);
					}
				}
			}
		}
	}
		
	private void reportEntitiesProcessed(int entitiesProcessed) {
		if(entitiesProcessed % 50000 == 0) {
			System.out.println("SessionClusterer: Entities processed: " + entitiesProcessed / 1000 + "k entities.");
		}
	}
	
	private void reportClassesProcessed(int classesProcessed) {
		if(classesProcessed % 50000 == 0) {
			System.out.println("SessionClusterer: Classes processed: " + classesProcessed / 1000 + "k entities.");
		}
	}
	
	private void reportSessionsProcessed(int sessionsProcessed) {
		if(sessionsProcessed % 10000 == 0) {
			long duration = (System.currentTimeMillis() - prevReportTime) / 1000;
			prevReportTime = System.currentTimeMillis();
			System.out.println("SessionClusterer: Sessions processed: " + sessionsProcessed / 1000 + "k entities. (" + duration + " s)");
		}
	}
	
}
