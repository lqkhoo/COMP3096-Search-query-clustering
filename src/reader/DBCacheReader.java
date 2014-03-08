package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBCacheReader {
	
	public DBCacheReader() {
		
	}
	
	public HashSet<String> readValidSearchStrings() {
		File dir = new File("output/db-cacher-out");
		
		File inputFile = new File(dir, "validSearchStrings.txt");
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		
		HashSet<String> validSearchStrings = new HashSet<String>();
		String line;
		
		// read file cache
		// validSearchStrings expects input to contain unique strings
		try {
			fileReader = new FileReader(inputFile);
			bufferedReader = new BufferedReader(fileReader);
			line = bufferedReader.readLine();
			while(line != null) {
				validSearchStrings.add(line);
				line = bufferedReader.readLine();
			}
		} catch (IOException e) {
			
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {}
		}
		
		return validSearchStrings;
	}
	
	public HashMap<String, int[]> readSearchStringToClassMappings() {
		File file = new File("output/db-cacher-out/searchStringsToClasses.tsv");
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		String line;
		
		HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, int[]> outputMap = new HashMap<String, int[]>();
		
		Pattern linePattern = Pattern.compile("((.*)[\\t](.*))");
		Matcher lineMatcher;
		String searchString;
		int clsNId;
		
		int linesRead = 0;
		
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			
			line = bufferedReader.readLine();
			while(line != null) {
				
				lineMatcher = linePattern.matcher(line);
				if(lineMatcher.find()) {
					searchString = lineMatcher.group(2);
					if(! lineMatcher.group(3).equals("null")) {
						clsNId = Integer.parseInt(lineMatcher.group(3));
						if(! map.containsKey(searchString)) {
							map.put(searchString, new ArrayList<Integer>());
						}
						map.get(searchString).add(clsNId);
					}
				}
				
				line = bufferedReader.readLine();
				
				linesRead++;
				if(linesRead % 50000 == 0) {
					System.out.println("DBCacheReader: Entities processed: " + linesRead / 1000 + "k lines.");
				}
				
			}
			
		} catch (IOException e) {
			
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {};
		}
		
		String[] keyset = map.keySet().toArray(new String[]{});
		ArrayList<Integer> array;
		int[] arrayOut;
		int arraySize;
		
		
		for(int i = 0; i < keyset.length; i++) {
			array = map.get(keyset[i]);
			arraySize = array.size();
			arrayOut = new int[arraySize];
			for(int j = 0; j < arraySize; j++) {
				arrayOut[j] = array.get(j);
			}
			outputMap.put(keyset[i], arrayOut);
		}
		
		return outputMap;
	}
	
}
