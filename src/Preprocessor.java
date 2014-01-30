import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * This is the preprocessor class handling all the data cleanup and time splitting
 * Takes AOL log files and outputs a set of JSON files containing session information
 *   in the form of an array of serialized SearchSession objects.
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
	public static final String DEFAULT_OUTPUT_DIR = "preprocessor-out/";
	private String outputDir;
	
	private LogReader logReader;
	private Cleaner cleaner;
	private BatchFileWriter writer;
	
	private SearchSession currentSession;
	private long sessionLength;
	private ArrayList<SearchSession> sessionArray;
	
	public Preprocessor() {
		this(DEFAULT_MAX_SESSIONS, DEFAULT_MAX_SESSION_LENGTH, DEFAULT_OUTPUT_DIR);
	}
	
	public Preprocessor(int maxSessions, long defaultMaxSessionLength, String outputDir) {
		this.maxSessions = maxSessions;
		this.maxSessionLength = defaultMaxSessionLength;
		this.outputDir = outputDir;
		this.logReader = new LogReader();
		this.cleaner = new Cleaner();
		this.writer = new BatchFileWriter(this.outputDir);
		
		this.currentSession = null;
		this.sessionLength = 0;
		this.sessionArray = new ArrayList<SearchSession>();
	}
	
	/*
	 * Write whatever's currently in sessionArray to file via BatchFileWriter 
	 */
	private void write() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(this.sessionArray);
		writer.writeToFile(json, "output"); // Write to file
	}
	
	private void timeSplit(LogObject logObj) {
		while(true) {
			// If new session, add the log data and we're finished
			if(currentSession == null) {
				currentSession = new SearchSession(logObj);
				return;
			} else {
				if(currentSession.getUserId() == logObj.getAnonId()) {
					sessionLength = logObj.getQueryTime().getTime() - currentSession.getSessionStart().getTime();
					if(sessionLength < maxSessionLength) { // If session length within normal bounds
						currentSession.addQuery(logObj.getQuery());
						currentSession.setSessionEnd(logObj.getQueryTime());
						return;
					}
				}
			}
			// Otherwise terminate existing session
			this.sessionArray.add(currentSession);
			if(this.sessionArray.size() >= maxSessions) {
				write();
				this.sessionArray = new ArrayList<SearchSession>(); // Reset sessionArray
			}
			currentSession = null; // Reset session, loop to beginning to write
		}
	}
	
	
	public void run() {
		
		// Clear output directory
		writer.deleteFilesInDir(this.outputDir);
		
		// Start processing
		LogObject obj = this.logReader.readNextLine();
		while(obj != null) {
			obj.setQuery(cleaner.filter(obj.getQuery()));
			if(! obj.getQuery().equals("")) {
				timeSplit(obj);
			}
			obj = this.logReader.readNextLine();
		}
		write();
		
	}
}
