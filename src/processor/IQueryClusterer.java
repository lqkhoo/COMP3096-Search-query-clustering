package processor;

import java.util.ArrayList;

import model.SearchSession;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public interface IQueryClusterer {
	
	//TODO
	public void cluster(ArrayList<SearchSession> searchSessions);
	//TODO read from file
	public void cluster();
	
}
