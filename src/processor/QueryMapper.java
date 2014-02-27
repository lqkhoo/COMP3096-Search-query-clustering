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

import lib.Stemmer;
import model.SearchSessionSerial;

import writer.MongoWriter;

/**
 * Writes query string mappings to the MongoDB collection called queryMap
 * @author Li Quan Khoo
 */
public class QueryMapper {
	
	public static final String DEFAULT_STOPWORDS_INPUT_FILE_PATH = "src/config/stopwords.ini";
	
	private PreprocessedLogReader logReader;
	private HashMap<String, String> stopwords;
	private MongoWriter mongoWriter;
	
	private Stemmer stemmer;
	
	private HashMap<String, String> matchResultsCache; // This is never refreshed
	private HashMap<String, String> noMatchCache; // This is refreshed every file to not overuse memory
	
	private long prevTime = System.currentTimeMillis();
	private long currentTime;
	private int updateCount = 0;
	
	public QueryMapper(MongoWriter mongoWriter) {
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
		
		// Maximum length of string in terms of substrings to search for in entity space.
		final int CULL_LENGTH = 3;
		
		ArrayList<String> substrings = new ArrayList<String>();
		String[] parts = query.split(" ");
		String substring;
		boolean containsStopword = false;
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
			
			if(j > CULL_LENGTH) {
				continue;
			}
			
			for(int i = 0; i + j < parts.length; i++) {
				substring = "";
				for(int k = i; k <= i + j; k++) {
					containsStopword = false;
					if(this.stopwords.containsKey(parts[k])) {
						containsStopword = true;
						break;
					}
					if(substring.equals("")) {
						substring += parts[k];
					} else {
						substring += " " + parts[k];
					}
				}
				
				// ignore stopwords, otherwise add to list
				if(! containsStopword) {
					substrings.add(substring);
				}
			}
		}
		
		return substrings.toArray(new String[]{});
		
	}
	
	private boolean getIsEntitySearchString(String queryString) {
		
		// Cache hit
		if(this.matchResultsCache.containsKey(queryString)) {
			return true;
		} else {
			if(this.noMatchCache.containsKey(queryString)) {
				return false;
			}
		}
		
		// Cache miss - db lookup
		DBObject entity = this.mongoWriter.getOneEntity(new BasicDBObject("searchString", queryString));
		
		// Cache results and return
		if(entity != null) {
			this.matchResultsCache.put(queryString, "");
			return true;

		} else {
			this.noMatchCache.put(queryString, "");
			return false;
		}
	}
	
	private void map(HashMap<String, Boolean> searchStringsHash, int sessionId) {
		
		String[] searchStrings = searchStringsHash.keySet().toArray(new String[]{});
		for(String searchString : searchStrings) {
			mongoWriter.addOrUpdateSearchMap(searchString, searchStrings, searchStringsHash, sessionId);
		}
	}
		
	public void run() {
		
		this.matchResultsCache = new HashMap<String, String>();
		
		SearchSessionSerial[] sessions = this.logReader.getLogs();
		int sessionId;
		String[] substrings;	// substrings formed from whole query string
		HashMap<String, Boolean> searchStringsHash;
		
		// for each file (100k sessions, ~ 20mb each on default settings)
		while(sessions != null) {
			
			System.out.println("QueryMapper: Processing sessions...");
			
			// Reset no-match cache after every file
			this.noMatchCache = new HashMap<String, String>();
			
			// for each session in file
			for(SearchSessionSerial session : sessions) {
				
				sessionId = session.getSessionId();
				searchStringsHash = new HashMap<String, Boolean>();
				
				// for each query in session
				for(String query : session.getQueries()) {
					
					if(query.contains(".")) { continue; } // If it has a dot it's most likely a URL - save on processing time and just ignore it
					
					substrings = generateQuerySubstrings(query);
					
					// for each query substring
					for(int i = 0; i < substrings.length; i++) {
						if(getIsEntitySearchString(substrings[i]) && ! searchStringsHash.containsKey(substrings[i])) {
							searchStringsHash.put(substrings[i], false);
						}
					}
				}
				
				map(searchStringsHash, sessionId);
				
				this.updateCount++;
				
				if(this.updateCount % 50000 == 0) {
					this.currentTime = System.currentTimeMillis();
					int seconds = (int) Math.floor((this.currentTime - this.prevTime) / 1000);
					this.prevTime = this.currentTime;
					System.out.println("QueryMapper: " + this.updateCount / 1000 + "k sessions processed (" + seconds + "s)");
				}
				
			}
			sessions = this.logReader.getLogs();
		}
	}
		
}
