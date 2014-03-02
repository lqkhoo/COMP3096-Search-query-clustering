package processor.yago;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;

import writer.MongoWriter;

/**
 * Specialized processor for the Yago file "yagoSimpleTypes"
 * 
 * @author Li Quan Khoo
 */
public class YagoSimpleTypesProcessor extends AYagoProcessor {
		
	public YagoSimpleTypesProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}
	
	@Override
	public void processEntitiesTsv() throws UnsupportedOperationException {
		String line;
		line = this.yagoReader.readNextLine_Tsv();
		
		Pattern linePattern = Pattern.compile("([\\t](.*)[\\t](.*)[\\t](.*)[\\t])?");
		Matcher lineMatcher;

		String name;
		String relation;
		String relationTarget;
		
		while(line != null) {
			lineMatcher = linePattern.matcher(line);
			if(lineMatcher.find()) {
				
				/*
				System.out.println(lineMatcher.group(1));	// the whole line
				System.out.println(lineMatcher.group(2));	// first
				System.out.println(lineMatcher.group(3));	// relation rdf:type
				System.out.println(lineMatcher.group(4));	// third 
				*/
				
				name = lineMatcher.group(2);
				relation = lineMatcher.group(3);
				relationTarget = lineMatcher.group(4);
				
				mongoWriter.addOrUpdateEntity(name, relation, relationTarget);
				
				/*
				if(mongoWriter.getEntity(new BasicDBObject("name", name)).isEmpty()) {
					System.out.println(name);
				}
				*/
				
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
	}
	
	@Override
	public void processClassesTsv() throws UnsupportedOperationException {
		String line;
		line = this.yagoReader.readNextLine_Tsv();
		
		Pattern linePattern = Pattern.compile("([\\t](.*)[\\t](rdf:type)[\\t](.*)[\\t])?");
		Matcher lineMatcher;

		String name;
		String relationTarget;
		
		HashMap<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();
		int memberArrayId = 0;
		
		while(line != null) {
			lineMatcher = linePattern.matcher(line);
			if(lineMatcher.find()) {
				
				name = lineMatcher.group(2);
				relationTarget = lineMatcher.group(4);
				
				if(! hash.containsKey(relationTarget)) {
					hash.put(relationTarget, new ArrayList<String>());
				}
				hash.get(relationTarget).add(name);
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
		
		System.out.println(hash.keySet().size());
		
		final int CLASS_MEMBER_ARRAY_SIZE = 50000;
		// Split entities list into arrays of 50,000 in length max and record references to them to bypass the 16Mb document size limit
		for(String key : hash.keySet().toArray(new String[]{})) {
			
			ArrayList<String> members = hash.get(key);
			List<String> partialArray;
			int i;
			
			for(i = 0; i < (members.size()) / CLASS_MEMBER_ARRAY_SIZE; i++) {
				mongoWriter.setClassMembers(key, members.subList(i * CLASS_MEMBER_ARRAY_SIZE, (i + 1) * CLASS_MEMBER_ARRAY_SIZE).toArray(new String[]{}), memberArrayId);
				memberArrayId++;
			}
			partialArray = members.subList(i * CLASS_MEMBER_ARRAY_SIZE, members.size() - 1);
			if(partialArray.size() != 0) {
				mongoWriter.setClassMembers(key, partialArray.toArray(new String[]{}), memberArrayId);
				memberArrayId++;
			}
			
		}
	}
	
	// Ttl not supported
}
