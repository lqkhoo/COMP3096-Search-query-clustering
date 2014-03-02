package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import reader.PreprocessedLogReader;

import model.SearchSessionSerial;

import writer.MongoWriter;

/**
 * Writes query string mappings to the MongoDB collection called queryMap
 * @author Li Quan Khoo
 */
@Deprecated
public class QueryMapperOld {
	
	public static final String DEFAULT_STOPWORDS_INPUT_FILE_PATH = "src/config/stopwords.ini";
	
	private PreprocessedLogReader logReader;
	private HashMap<String, String> stopwords;
	private MongoWriter mongoWriter;
	
	
	private HashMap<String, String[]> matchResultsCache; // This is never refreshed
	private HashMap<String, String> noMatchCache; // This is refreshed every file to not overuse memory
	
	public QueryMapperOld(MongoWriter mongoWriter) {
		this.logReader = new PreprocessedLogReader();
		this.mongoWriter = mongoWriter;
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
	
	private String[] generateQuerySubstrings(String query) {
		ArrayList<String> substrings = new ArrayList<String>();
		String[] parts = query.split(" ");
		String substring;
		
		// degenerate
		if(query.equals("") || query == null) {
			return new String[] {};
		}
		
		//          0     1     2     3
		// String [***] [***] [***] [***]
		//          ^				  ^
		//          i				  j
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
				
				// ignore stopwords, otherwise add to list
				if(! this.stopwords.containsKey(substring)) {
					substrings.add(substring);
				}
			}
		}
		
		return substrings.toArray(new String[]{});
		
	}
	
	private String[] getMatchingEntityNames(String queryString) {
		
		// Cache hit
		if(this.matchResultsCache.containsKey(queryString)) {
			return this.matchResultsCache.get(queryString);
		} else {
			if(this.noMatchCache.containsKey(queryString)) {
				return new String[]{};
			}
		}
		
		// Cache miss - db lookup
		ArrayList<DBObject> entities = this.mongoWriter.getEntities(new BasicDBObject("searchString", queryString));
		
		// Cache results and return
		ArrayList<String> entityNames;
		String[] entityNamesArray;
		if(entities.size() != 0) {
			entityNames = new ArrayList<String>();
			for(int i = 0; i < entities.size(); i++) {
				entityNames.add((String) entities.get(i).get("name"));
			}
			entityNamesArray = entityNames.toArray(new String[]{});
			this.matchResultsCache.put(queryString, entityNamesArray);
			return entityNamesArray;
		} else {
			this.noMatchCache.put(queryString, "");
			return new String[]{};
		}
	}
	
	private void map(HashMap<String, Boolean> entityNamesHash, int sessionId) {
		
		String[] entityNames = entityNamesHash.keySet().toArray(new String[]{});
		for(String entityName : entityNames) {
			System.out.println(entityName);
			// mongoWriter.addOrUpdateEntityMap(entityName, entityNames, entityNamesHash, sessionId);
		}
	}
		
	public void run() {
		
		this.matchResultsCache = new HashMap<String, String[]>();
		
		SearchSessionSerial[] sessions = this.logReader.getLogs();
		int sessionId;
		String[] substrings;	// substrings formed from whole query string
		String[] entityNames;	// names of entities matching query string
		boolean isFullMatch;	// Whether the match is to the whole query string or to one of its substrings
		HashMap<String, Boolean> entityNamesHash;
		
		// for each file (100k sessions, ~ 20mb each on default settings)
		while(sessions != null) {
			
			// Reset no-match cache after every file
			this.noMatchCache = new HashMap<String, String>();
			
			// for each session in file
			for(SearchSessionSerial session : sessions) {
				
				sessionId = session.getSessionId();
				entityNamesHash = new HashMap<String, Boolean>();
				
				// for each query in session
				for(String query : session.getQueries()) {
					
					if(query.contains(".")) { continue; } // If it has a dot it's most likely a URL - save on processing time and just ignore it
					
					substrings = generateQuerySubstrings(query);
					
					// for each query substring
					for(int i = 0; i < substrings.length; i++) {
						isFullMatch = (i == 0) ? true : false;
						entityNames = getMatchingEntityNames(substrings[i]);
						for(String name : entityNames) {
							if(! entityNamesHash.containsKey(name)) {
								entityNamesHash.put(name, isFullMatch);
							}
						}
					}
				}
				
				// Once we have all the entity names, construct an n x n strongly connected graph
				map(entityNamesHash, sessionId);
				
			}
			sessions = this.logReader.getLogs();
		}
	}
		
}
