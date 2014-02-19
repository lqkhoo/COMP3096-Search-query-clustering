package processor.yago;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public void processTsv() throws UnsupportedOperationException {
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
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
	}
	
	// Ttl not supported
}
