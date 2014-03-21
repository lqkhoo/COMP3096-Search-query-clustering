package processor;

import java.util.HashSet;

import model.SearchSessionSerial;
import reader.PreprocessedLogReader;
import writer.MongoWriter;

/**
 * 
 * @author Li Quan Khoo
 * 
 * Stopgap class to augment semantic session with the original query string.
 * This class should be fused into QueryMapper / SessionClusterer ideally, but we have 2 days to go so this has to be it.
 */
public class NaiveSearchStringToSemanticSession {
	
	MongoWriter mongoWriter;
	PreprocessedLogReader logReader;
	
	long prevReportTime = System.currentTimeMillis();
	
	public NaiveSearchStringToSemanticSession(MongoWriter mongoWriter) {
		this.mongoWriter = mongoWriter;
		this.logReader = new PreprocessedLogReader();
	}
	
	public void run() {
		
		SearchSessionSerial[] sessions = this.logReader.getLogs();
		
		int sessionId;
		String[] queries;
		HashSet<String> queryHash;
		int sessionsProcessed = 0;
		
		while(sessions != null) {
			
			for(SearchSessionSerial session : sessions) {
				
				sessionId = session.getSessionId();
				queries = session.getQueries();
				queryHash = new HashSet<String>();
				
				for(String query : queries) {
					queryHash.add(query);
				}
				
				this.mongoWriter.setSemanticSessionQueries(sessionId, queryHash.toArray(new String[]{}));
				sessionsProcessed++;
				if(sessionsProcessed % 10000 == 0) {
					long duration = (System.currentTimeMillis() - prevReportTime) / 1000;
					prevReportTime = System.currentTimeMillis();
					System.out.println("SessionClusterer: Sessions processed: " + sessionsProcessed / 1000 + "k entities. (" + duration + " s)");
				}
			}
			
			sessions = this.logReader.getLogs();
		}
		
	}
	
}
