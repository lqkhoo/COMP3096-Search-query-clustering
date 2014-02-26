package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import reader.PreprocessedLogReader;

import com.google.gson.Gson;

import model.SearchSessionSerial;

import writer.MongoWriter;

/**
 * Writes query string mappings to the MongoDB collection called queryMap
 * @author Li Quan Khoo
 */
public class QueryMapper {
	
	private PreprocessedLogReader logReader;
	private String[] stopWords;
	private MongoWriter mongoWriter;
	
	public QueryMapper() {
		this.logReader = new PreprocessedLogReader();
	}
	
	public void run() {
		SearchSessionSerial[] sessions = this.logReader.getLogs();
		while(sessions != null) {
			for(SearchSessionSerial session : sessions) {
				map(session);
			}
			sessions = this.logReader.getLogs();
		}
	}
	
	public void map(SearchSessionSerial session) {
		
	}
	
}
