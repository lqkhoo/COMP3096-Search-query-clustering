package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import lib.Stemmer;
import model.SearchSessionSerial;
import model.SessionSearchStringMapping;

import reader.DBCacheReader;
import reader.PreprocessedLogReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	private HashMap<String, String> stopwords;
	
	// Maps searchString to a list of their class nIds
	private HashMap<String, int[]> searchStringMap = new HashMap<String, int[]>();
	
	// Maps classes with sessionIds, taking into account mapping strength
	//private SessionToClassMapping sessionMap;
	private HashMap<Integer, String[]> sessionMap;
	
	long prevReportTime = System.currentTimeMillis();
	
	public SessionClusterer(MongoWriter mongoWriter, String sessionMapMode) {
		this.logReader = new PreprocessedLogReader();
		this.mongoWriter = mongoWriter;
		this.stemmer = new Stemmer();
		this.sessionMap = new HashMap<Integer, String[]>();
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
		loadSearchStringsToClassMappingsFromFile();
		processSessions();
		// sessionMapToFile();	// uncomment this line if you want file output alongside db output
		usefulSessionsToDB();
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
						if(this.searchStringMap.containsKey(substring)) {
							validSearchStrings.put(substring, true);
							removeSubstrings(substrings, substring);
							
						} else {
							stemmedSearchString = stemQueryString(substring);
							if(this.searchStringMap.containsKey(stemmedSearchString)) {
								validSearchStrings.put(stemmedSearchString, true);
								removeSubstrings(substrings, substring); // remove the originals, not the stemmed ones
							}
						} // else do nothing
					}
					
					this.sessionMap.put(sessionId, validSearchStrings.keySet().toArray(new String[]{}));
					
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
		
	private void usefulSessionsToDB() {
		String[] searchStrings;
		
		int sessionsWritten = 0;
		
		this.batchFileWriter = new BatchFileWriter("output/clusterer-out/sessions", "json");
		this.batchFileWriter.deleteFilesInDir();
		
		for(int sessionId : this.sessionMap.keySet().toArray(new Integer[]{})) {
			searchStrings = this.sessionMap.get(sessionId);
			
			// set to != 0 to include sessions with only one searchString match
			if(searchStrings.length > 1) {
				this.mongoWriter.addOrUpdateUsefulSession(sessionId, searchStrings);
			}
			
			reportSessionsWrittenToDb(++sessionsWritten);
		}
	}
	
	private void sessionMapToFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		int listLength = 0;
		ArrayList<SessionSearchStringMapping> mappings;
		String[] searchStrings;
		
		this.batchFileWriter = new BatchFileWriter("output/clusterer-out/sessions", "json");
		this.batchFileWriter.deleteFilesInDir();
		
		mappings = new ArrayList<SessionSearchStringMapping>();
		for(int sessionId : this.sessionMap.keySet().toArray(new Integer[]{})) {
			searchStrings = this.sessionMap.get(sessionId);
			
			if(searchStrings.length != 0) {
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
	
	private void reportSessionsProcessed(int sessionsProcessed) {
		if(sessionsProcessed % 10000 == 0) {
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
