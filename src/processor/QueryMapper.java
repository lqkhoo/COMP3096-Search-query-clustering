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
	
	private HashMap<String, String> matchResultsCache;
	private HashMap<String, String> noMatchCache;
	
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
				
		ArrayList<String> substrings = new ArrayList<String>();
		String[] parts = query.split(" ");
		String substring;
		
		// degenerate
		if(query.equals("") || query == null) {
			return new String[] {};
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
		
		return substrings.toArray(new String[]{});
		
	}
	
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
	
	private String lookupEntitySearchStringAndCache(String queryString) {
		DBObject entity = this.mongoWriter.getOneEntity(new BasicDBObject("searchString", queryString));
		
		// Cache results and return
		if(entity != null) {
			this.matchResultsCache.put(queryString, "");
			return queryString;

		} else {
			this.noMatchCache.put(queryString, "");
			return null;
		}
	}
	
	private String getEntitySearchString(String queryString) {
		
		// Cache hit
		if(this.matchResultsCache.containsKey(queryString)) {
			return queryString;
		} else {
			if(this.noMatchCache.containsKey(queryString)) {
				String stemmedQueryString = stemQueryString(queryString);
				return lookupEntitySearchStringAndCache(stemmedQueryString);
			}
			
			return lookupEntitySearchStringAndCache(queryString);
		}
		
		// Cache miss - db lookup

	}
	
	private void map(HashMap<String, Boolean> searchStringsHash, int sessionId) {
		
		String[] searchStrings = searchStringsHash.keySet().toArray(new String[]{});
		for(String searchString : searchStrings) {
			mongoWriter.addOrUpdateSearchMap(searchString, searchStrings, searchStringsHash, sessionId);
		}
	}
		
	public void run() {
				
		SearchSessionSerial[] sessions = this.logReader.getLogs();
		int sessionId;
		String[] substrings;	// substrings formed from whole query string
		String searchString;
		HashMap<String, Boolean> searchStringsHash;
		
		// for each file (100k sessions, ~ 20mb each on default settings)
		while(sessions != null) {
			
			System.out.println("QueryMapper: Processing sessions...");
			
			// Reset cache after every file
			this.matchResultsCache = new HashMap<String, String>();
			this.noMatchCache = new HashMap<String, String>();
			
			// for each session in file
			for(SearchSessionSerial session : sessions) {
				
				sessionId = session.getSessionId();
				searchStringsHash = new HashMap<String, Boolean>();
				
				// for each query in session
				for(String query : session.getQueries()) {
					
					substrings = generateQuerySubstrings(query);
					
					// for each query substring
					for(int i = 0; i < substrings.length; i++) {
						searchString = getEntitySearchString(substrings[i]);
						if(searchString != null && ! searchStringsHash.containsKey(searchString)) {
							searchStringsHash.put(searchString, false);
						}
					}
				}
				
				map(searchStringsHash, sessionId);
				
				this.updateCount++;
				
				if(this.updateCount % 10000 == 0) {
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
