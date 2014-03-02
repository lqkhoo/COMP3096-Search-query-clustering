package processor.yago;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import writer.MongoWriter;

public class YagoWordnetDomainsProcessor extends AYagoProcessor {
	
	public YagoWordnetDomainsProcessor(MongoWriter mongoWriter, String inputFilePath, String inputFileType) {
		super(mongoWriter, inputFilePath, inputFileType);
	}
	
	@Override
	public void processEntitiesTsv() throws UnsupportedOperationException {
		String line;
		line = this.yagoReader.readNextLine_Tsv();
		
		Pattern linePattern = Pattern.compile("((.*)[\\t](.*)[\\t](.*)[\\t](.*)[\\t])");
		Matcher lineMatcher;
		
		String name;
		String relation;
		String relationTarget;
		
		Pattern pattern;
		Matcher matcher;
		
		String cleanName;
		String searchString;
		String disambig = "";
				
		while(line != null) {
			lineMatcher = linePattern.matcher(line);
			if(lineMatcher.find()) {
				
				name = lineMatcher.group(3);
				relation = lineMatcher.group(4);
				relationTarget = lineMatcher.group(5);
				
				pattern = Pattern.compile("(<wordnet_(.*)_(.*)>)");
				disambig = "";
				matcher = pattern.matcher(name);
				
				if(matcher.find()) {
					cleanName = matcher.group(2).replace("_", " ").replace("<", "").replace(">", "");
					searchString = cleanName.toLowerCase();
					
					/*
					System.out.println(name);
					System.out.println(cleanName);
					System.out.println(disambig);
					System.out.println(searchString);
					System.out.println(relation);
					System.out.println(relationTarget); 
					*/
					
					mongoWriter.addOrUpdateEntity(name, cleanName, disambig, searchString, relation, relationTarget);
				}
				
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
	}
	
}
