package writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import model.mapping.SearchStringToClassMapping;

public class DBCacheWriter {
	
	public DBCacheWriter() {
		
	}
	
	public void writeValidSearchStrings(String[] strings) {
		File dir = new File("output/db-cacher-out");
		File file = new File(dir, "validSearchStrings.txt");
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		try {
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			for(int i = 0; i < strings.length; i++) {
				bufferedWriter.append(strings[i] + "\n");
			}
			
		} catch (IOException e) {
			
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {};
		}
	}
	
	public void writeSearchStringToClassMappings(SearchStringToClassMapping mapping) {
		
		File dir = new File("output/db-cacher-out");
		File outputFile = new File(dir, "searchStringsToClasses.tsv");
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		
		try {
			fileWriter = new FileWriter(outputFile, true);
			bufferedWriter = new BufferedWriter(fileWriter);
			
			for(int i = 0; i < mapping.classNIds.length; i++) {
				bufferedWriter.append(mapping.searchString + "\t" + mapping.classNIds[i] + "\n");
			}
			
		} catch (IOException e) {
			
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {};
		}
	}
	
}
