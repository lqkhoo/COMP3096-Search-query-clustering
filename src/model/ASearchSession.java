package model;

import java.util.Date;

public abstract class ASearchSession {
	
	protected Date start;
	protected Date end;
	protected int sessionId;	// own given numeric id (incremental) to identify each session
	protected transient int userId;
		
	public abstract void addQuery(String queryString);
	public abstract String[] getQueries();
	
	public Date getSessionStart() { return this.start; }
	public void setSessionStart(Date sessionStart) { this.start = sessionStart; }
	public Date getSessionEnd() { return this.end; }
	public void setSessionEnd(Date sessionEnd) { this.end = sessionEnd; }
	public int getUserId() { return this.userId; }
	public int getSessionId() { return this.sessionId; }
	public void setSessionId(int sessionId) { this.sessionId = sessionId; }
	
}
