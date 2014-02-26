package processor;
import java.util.ArrayList;

import reader.AolLogReader;
import writer.BatchFileWriter;

import model.LogObject;
import model.SearchSessionSerial;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * This is the preprocessor class handling all the data cleanup and time splitting
 * Takes AOL log files and outputs a set of JSON files containing session information
 *   in the form of an array of serialized SearchSession objects.
 * The output of these files are called Time-gap sessions within Lucchese et al. 2011
 * @author Li Quan Khoo
 *
 */
public class Preprocessor {
	
	// This is the number of sessions preprocessed before triggering the class to close the current file as soon as the current
	//   session has been finished recording and start writing to a new one.
	public static final int DEFAULT_MAX_SESSIONS = 100000;
	private int maxSessions;
	
	// Timesplitter (TS-x) algorithm session segmentation threshold
	public static final long DEFAULT_MAX_SESSION_LENGTH = 26 * 60 * 1000; // milliseconds
	private long maxSessionLength;
	
	// Default write directory
	public static final String DEFAULT_OUTPUT_DIR = "output/preprocessor-out/";
	private String outputDir;
	
	public static final double NORMALIZED_QUERY_DISTANCE_SEGMENTATION_THRESHOLD = 0.3;
	
	private AolLogReader logReader;
	private Cleaner cleaner;
	private BatchFileWriter writer;
	
	private SearchSessionSerial currentSession;
	private long sessionLength;
	private ArrayList<SearchSessionSerial> sessionArray;
	
	private int sessionIdStart;
	private int currentSessionId;
	
	public Preprocessor() {
		this(DEFAULT_MAX_SESSIONS, DEFAULT_MAX_SESSION_LENGTH, DEFAULT_OUTPUT_DIR);
	}
	
	public Preprocessor(int maxSessions, long defaultMaxSessionLength, String outputDir) {
		this.maxSessions = maxSessions;
		this.maxSessionLength = defaultMaxSessionLength;
		this.outputDir = outputDir;
		this.logReader = new AolLogReader();
		this.cleaner = new Cleaner();
		this.writer = new BatchFileWriter(this.outputDir, "json");
		
		this.currentSession = null;
		this.sessionLength = 0;
		this.sessionArray = new ArrayList<SearchSessionSerial>();
		this.sessionIdStart = 0;
		this.currentSessionId = this.sessionIdStart;
	}
	
	/*
	 * Write whatever's currently in sessionArray to file via BatchFileWriter 
	 */
	private void write() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(this.sessionArray);
		writer.writeToFile(json, "output"); // Write to file
	}
	
	/**
	 * Returns a boolean of whether to split the session based on time or not.
	 * True means split. False means don't 
	 */
	private boolean timeSplit(LogObject logLine) {
		
		// If current session's user is different to current log's user then definitely split
		if(currentSession.getUserId() != logLine.getAnonId()) {
			return true;
		}
		
		sessionLength = logLine.getQueryTime().getTime() - currentSession.getSessionStart().getTime();
		
		// If session length is within bounds, don't split
		if(sessionLength < maxSessionLength) {
			return false;
		}
		
		// Otherwise split
		return true;
	}
	
	private boolean queryDistance(LogObject logLine, String previousCleanedQuery) {
		
		double queryDistance;
		
		if(previousCleanedQuery == null) {
			return false;
		} else {
			queryDistance = QueryDistance.distance(logLine.getCleanedQuery(), previousCleanedQuery);
			if(queryDistance > 1 - NORMALIZED_QUERY_DISTANCE_SEGMENTATION_THRESHOLD) {
				return true;
			}
		}
		return false;
	}
	
	private void newSearchSession(LogObject logLine) {
		this.currentSession = new SearchSessionSerial(this.currentSessionId, logLine);
		this.currentSessionId++;
	}
	
	public void run() {
		
		LogObject logLine;
		String previousCleanedQuery;
		boolean split;
		
		// Clear output directory
		writer.deleteFilesInDir(this.outputDir);
		
		// Start processing
		previousCleanedQuery = null;
		logLine = this.logReader.readNextLine();
		
		while(logLine != null) {
			split = false;
			logLine.setCleanedQuery(cleaner.filter(logLine.getQuery()));
			
			// If query is not nonsense
			if(! logLine.getCleanedQuery().equals("")) {
				
				// If current session is not defined, create a new one
				if(currentSession == null) {
					newSearchSession(logLine);
					previousCleanedQuery = logLine.getCleanedQuery();
				} else {
					
					// Otherwise a session is already in place. Run timesplit() and distance() to determine whether
					//   to start a new session or maintain the current one
					split = timeSplit(logLine) || queryDistance(logLine, previousCleanedQuery);
					
					previousCleanedQuery = logLine.getCleanedQuery();
					if(! split) {
						currentSession.addQuery(logLine.getQuery());
						currentSession.setSessionEnd(logLine.getQueryTime());
					} else {
						
						this.sessionArray.add(currentSession);
						if(this.sessionArray.size() >= maxSessions) {
							
							// do stuff with the full array of sessions here
							write();
							
							this.sessionArray = new ArrayList<SearchSessionSerial>(); // Reset sessionArray
						}
						
						newSearchSession(logLine);
						previousCleanedQuery = logLine.getCleanedQuery();
					}
				}
			}
			
			logLine = this.logReader.readNextLine();
		}
		
		if(this.sessionArray.size() != 0) {
			// do stuff with the partially full array of the last sessions here
			write();
		}
		
	}
}
