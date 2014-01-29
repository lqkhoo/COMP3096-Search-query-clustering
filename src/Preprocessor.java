import java.util.ArrayList;


/**
 * This is the preprocessor class handling all the data cleanup
 * @author Li Quan Khoo
 *
 */
public class Preprocessor {
	
	// This is the number of sessions preprocessed before tringgering the class to close the current file as soon as the current
	//   session has been finished recording and start writing to a new one.
	public static final int DEFAULT_MAX_SESSIONS = 3;
	private int maxSessions;
	
	// Timesplitter (TS-x) algorithm session segmentation threshold
	public final static long DEFAULT_MAX_SESSION_LENGTH = 26 * 60 * 1000; // milliseconds
	private long maxSessionLength;
	
	private LogReader logReader;
	private Cleaner cleaner;
	
	private SearchSession currentSession;
	private long sessionLength;
	private ArrayList<SearchSession> sessionArray;
	
	
	public Preprocessor() {
		this(DEFAULT_MAX_SESSIONS, DEFAULT_MAX_SESSION_LENGTH);
	}
	
	public Preprocessor(int maxSessions, long defaultMaxSessionLength) {
		this.maxSessions = maxSessions;
		this.maxSessionLength = defaultMaxSessionLength;
		this.logReader = new LogReader();
		this.cleaner = new Cleaner();
		
		this.currentSession = null;
		this.sessionLength = 0;
		this.sessionArray = new ArrayList<SearchSession>();
	}

	private void timeSplit(LogObject obj) {
		
		while(true) {
			// If new session, add the log data and we're finished
			if(currentSession == null) {
				currentSession = new SearchSession(obj.getAnonId(), obj.getQueryTime());
				currentSession.addQuery(obj.getQuery());
				currentSession.setSessionEnd(obj.getQueryTime());
				return;
			} else {
				// Existing session
				if(currentSession.getUserId() == obj.getAnonId()) {
					sessionLength = obj.getQueryTime().getTime() - currentSession.getSessionStart().getTime();
					// If session length within normal bounds
					if(sessionLength < maxSessionLength) {
						currentSession.addQuery(obj.getQuery());
						currentSession.setSessionEnd(obj.getQueryTime());
						return;
					}
				}
				
				// Otherwise terminate existing session
				this.sessionArray.add(currentSession);
				
				if(this.sessionArray.size() >= maxSessions) {
					// write to file, reset sessionArray
					//TODO
					this.sessionArray = new ArrayList<SearchSession>();
				}
				
				// Reset session, loop to beginnning to write
				currentSession = null;
				
			}
		}
	}
	
	
	public void run() {
		
		LogObject obj = this.logReader.readNextLine();
		while(obj != null) {
			
			obj.setQuery(cleaner.filter(obj.getQuery()));
			if(! obj.getQuery().equals("")) {
				timeSplit(obj);
				//System.out.println(obj.toString());
				
			}
			obj = this.logReader.readNextLine();
		}
	}
	
}
