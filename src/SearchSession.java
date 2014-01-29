import java.util.ArrayList;
import java.util.Date;

/**
 * Container class representing a search session
 * @author Li Quan Khoo
 *
 */
public class SearchSession {
	
	private Date sessionStart;
	private Date sessionEnd;
	private int userId;
	private ArrayList<String> queries;
	
	public SearchSession() {
		this(-1, null);
	}
	
	public SearchSession(int userId, Date startTime) {
		this.queries = new ArrayList<String>();
		this.userId = userId;
		this.sessionStart = null;
		this.sessionEnd = null;
	}
	
	public void addQuery(String queryString) {
		this.queries.add(queryString);
	}
	
	public Date getSessionStart() {
		return this.sessionStart;
	}
	
	public void setSessionStart(Date sessionStart) {
		this.sessionStart = sessionStart;
	}
	
	public Date getSessionEnd() {
		return this.sessionEnd;
	}
	
	public void setSessionEnd(Date sessionEnd) {
		this.sessionEnd = sessionEnd;
	}
	
	public int getUserId() {
		return this.userId;
	}
	
}
