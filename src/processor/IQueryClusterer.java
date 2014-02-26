package processor;

import java.util.ArrayList;

import model.SearchSessionSerial;

/**
 * 
 * @author Li Quan Khoo
 *
 */
public interface IQueryClusterer {
	
	//TODO
	public void cluster(ArrayList<SearchSessionSerial> searchSessions);
	//TODO read from file
	public void cluster();
	
}
