package processor.yago;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.YagoHierarchy;

public class YagoTaxonomyProcessor extends AYagoProcessor {
	
	public YagoTaxonomyProcessor(YagoHierarchy hierarchy, String inputFilePath, String inputFileType) {
		super(hierarchy, inputFilePath, inputFileType);
	}
	
	@Override
	public void processTsv() throws UnsupportedOperationException {
		String line;
		line = this.yagoReader.readNextLine_Tsv();
		
		// schema: <id_nonsense>? \t <wikicategory/wordnet_abcde> \t rdfs:subClassOf \t <wordnet_abcde>
		Pattern linePattern = Pattern.compile("((.*)?[\\t](.*)[\\t](.*)[\\t](.*)[\\t])");
		Matcher lineMatcher;

		String name;
		String relation;
		String relationTarget;
		
		while(line != null) {
			lineMatcher = linePattern.matcher(line);
			if(lineMatcher.find()) {
				
				name = lineMatcher.group(3);
				relation = lineMatcher.group(4);
				relationTarget = lineMatcher.group(5);
				
				/*
				System.out.println(name);
				System.out.println(relation);
				System.out.println(relationTarget);
				*/
				
				// No writing to mongo!!
				hierarchy.addRelation(name, relationTarget);
			}
			line = this.yagoReader.readNextLine_Tsv();
		}
		
	}
	
}
