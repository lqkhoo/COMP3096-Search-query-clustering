import java.util.Date;
import java.util.HashMap;

/**
 * Container class representing a search session
 * @author Li Quan Khoo
 *
 */
public class SearchSession {
	
	private Date start;
	private Date end;
	private int userId;
	private HashMap<String, Integer> queries;
	
	public SearchSession() {
		this(-1, null);
	}
	
	public SearchSession(int userId, Date startTime) {
		this.queries = new HashMap<String, Integer>();
		this.userId = userId;
		this.start = startTime;
		this.end = startTime;
	}
	
	public SearchSession(LogObject logObject) {
		this(logObject.getAnonId(), logObject.getQueryTime());
		this.queries.put(logObject.getQuery(), 1);
	}
	
	public void addQuery(String queryString) {
		if(this.queries.containsKey(queryString)) {
			this.queries.put(queryString, this.queries.get(queryString) + 1);
		} else {
			this.queries.put(queryString, 1);
		}
	}
	public Date getSessionStart() { return this.start; }
	public void setSessionStart(Date sessionStart) { this.start = sessionStart; }
	public Date getSessionEnd() { return this.end; }
	public void setSessionEnd(Date sessionEnd) { this.end = sessionEnd; }
	public int getUserId() { return this.userId; }
	
}
