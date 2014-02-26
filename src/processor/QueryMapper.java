package processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;

import model.SearchSessionSerial;

import writer.MongoWriter;

/**
 * Writes query string mappings to the MongoDB collection called queryMap
 * @author Li Quan Khoo
 */
public class QueryMapper {
	
	public static final String DEFAULT_INPUT_DIR = "output/preprocessor-out/";
	private String inputDir;
	
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	
	private String[] stopWords;
	
	private MongoWriter mongoWriter;
	
	public QueryMapper() {
		this(DEFAULT_INPUT_DIR);
	}
	
	public QueryMapper(String inputDir) {
		this.inputDir = inputDir;
	}
	
	//TODO batch processing
	private SearchSessionSerial[] readFile() {
		
		StringBuilder stringBuilder = new StringBuilder();
		File file = new File(this.inputDir + "output-0.json");
		try {
			String line;
			
			this.fileReader = new FileReader(file);
			this.bufferedReader = new BufferedReader(this.fileReader);
			
			line = this.bufferedReader.readLine();
			while(line != null) {
				stringBuilder.append(line);
				line = this.bufferedReader.readLine();
			}
			
		} catch(FileNotFoundException e) {
			
		} catch (IOException e) {
			
		} finally {
			if(this.bufferedReader != null) {
				try {
					this.bufferedReader.close();
				} catch(IOException e) {}
			}
		}
		
		return new Gson().fromJson(stringBuilder.toString(), new SearchSessionSerial[]{}.getClass());
	}
	
	/*
	private String[] getQuerySubstrings(String queryString) {
		
	}
	*/
	
	public void map() {
		
		
		
		SearchSessionSerial[] sessions = this.readFile();
		
		for(SearchSessionSerial session : sessions) {
			for(String query : session.getQueries()) {
				
				
				
			}
			
			
		}
	}
	
}
