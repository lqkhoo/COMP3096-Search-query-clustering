package model;

public class SessionSearchStringMapping {
	
	public final int sessionId;
	public final String[] searchStrings;
	
	public SessionSearchStringMapping(int sessionId, String[] searchStrings) {
		this.sessionId = sessionId;
		this.searchStrings = searchStrings;
	}
}
