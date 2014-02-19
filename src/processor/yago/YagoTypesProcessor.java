package processor.yago;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import writer.MongoWriter;

/**
 * Specialized processor for the Yago file "yagoTypes"
 * 
 * @author Li Quan Khoo
 */
public class YagoTypesProcessor extends AYagoProcessor {
	
	public YagoTypesProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}
	
	@Override
	public void processTsv() throws UnsupportedOperationException {
		String line;
		line = this.yagoReader.readNextLine_Tsv();
		
		// schema: <id_foo_bar_baz> \t <entity> \t rdf:type \t <wordnet / wikicategory / etc>
		Pattern linePattern = Pattern.compile("([\\t](.*)[\\t](.*)[\\t](.*)[\\t](.*))");
		Matcher lineMatcher;

		String name;
		String relation;
		String relationTarget;
		
		while(line != null) {
			lineMatcher = linePattern.matcher(line);
			if(lineMatcher.find()) {
								
				name = lineMatcher.group(2);
				relation = lineMatcher.group(3);
				relationTarget = lineMatcher.group(4);
				
				/*
				System.out.println(name);
				System.out.println(relation);
				System.out.println(relationTarget);
				*/
				
				mongoWriter.addOrUpdateEntity(name, relation, relationTarget);
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
	}
	
	// Ttl not supported
	
}
