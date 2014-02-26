package processor.yago;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;

import writer.MongoWriter;

/**
 * 
 * Specialized processor for the yago file "yagoLabels"
 * @author Li Quan Khoo
 */
public class YagoLabelsProcessor extends AYagoProcessor {
	
	public YagoLabelsProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}
	
	@Override
	public void processTsv() throws UnsupportedOperationException {
		String line;
		line = this.yagoReader.readNextLine_Tsv();
		
		Pattern linePattern = Pattern.compile("((.*)?[\\t](.*)[\\t](.*)[\\t](.*))[\\t]");
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
				
				name = lineMatcher.group(3);
				relation = lineMatcher.group(4);
				relationTarget = lineMatcher.group(5);
				
				/*
				System.out.println(name);
				System.out.println(relation);
				System.out.println(relationTarget);
				*/
				if(mongoWriter.getEntity(new BasicDBObject("name", name)).isEmpty()) {
					System.out.println(name);
				}
				
				/*
				mongoWriter.addOrUpdateEntity(name, relation, relationTarget);
				*/
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
	}
}
