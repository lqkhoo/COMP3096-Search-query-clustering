package model;

import java.util.Date;
import java.util.HashMap;

/**
 * Container class representing a search session
 * @author Li Quan Khoo
 *
 */
@Deprecated
public class SearchSessionHash extends ASearchSession {
	
	private HashMap<String, Integer> queries;
	
	public SearchSessionHash(int sessionId) {
		this(sessionId, -1, null);
	}
	
	public SearchSessionHash(int sessionId, int userId, Date startTime) {
		this.queries = new HashMap<String, Integer>();
		this.userId = userId;
		this.start = startTime;
		this.end = startTime;
		this.sessionId = sessionId;
	}
	
	public SearchSessionHash(int sessionId, LogObject logObject) {
		this(sessionId, logObject.getAnonId(), logObject.getQueryTime());
		this.queries.put(logObject.getQuery(), 1);
	}
	
	@Override
	public void addQuery(String queryString) {
		if(this.queries.containsKey(queryString)) {
			this.queries.put(queryString, this.queries.get(queryString) + 1);
		} else {
			this.queries.put(queryString, 1);
		}
	}
	
	@Override
	public String[] getQueries() {
		return this.queries.keySet().toArray(new String[]{});
	}
	
}
