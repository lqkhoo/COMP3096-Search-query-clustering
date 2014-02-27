package model;

import java.util.ArrayList;
import java.util.Date;

/**
 * Container class representing a search session
 * @author Li Quan Khoo
 *
 */
public class SearchSessionSerial extends ASearchSession {
	
	// Using ArrayList instead of HashMap to support QC-htc query clusterer, which 
	//  needs sequential queries to function properly
	private ArrayList<String> queries;
	
	public SearchSessionSerial(int sessionId) {
		this(sessionId, -1, null);
	}
	
	public SearchSessionSerial(int sessionId, int userId, Date startTime) {
		this.queries = new ArrayList<String>();
		this.userId = userId;
		this.start = startTime;
		this.end = startTime;
		this.sessionId = sessionId;
	}
	
	public SearchSessionSerial(int sessionId, LogObject logObject) {
		this(sessionId, logObject.getAnonId(), logObject.getQueryTime());
		this.queries.add(logObject.getQuery());
	}
	
	@Override
	public void addQuery(String queryString) {
		this.queries.add(queryString);
	}
	
	@Override
	public String[] getQueries() {
		return this.queries.toArray(new String[]{});
	}
	
}
