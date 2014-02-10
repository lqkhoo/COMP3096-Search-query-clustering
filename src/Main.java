import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processor.IQueryClusterer;
import processor.Preprocessor;
import processor.QChtc;
import processor.YagoProcessor;
import processor.yago.AYagoProcessor;
import processor.yago.YagoTypesProcessor;
import reader.BigFileSampler;


public class Main {
	
	private static Preprocessor preprocessor;
	private static IQueryClusterer clusterer;
	private static BigFileSampler sampler;
	private static YagoProcessor yagoProcessor;
	
	/**
	 * Preprocessor - Take logs and output segmented JSON search session objects
	 * 
	 */
	private static void preprocessQueryLogs() {
		preprocessor = new Preprocessor();
		preprocessor.run();
	}
	
	/**
	 * Query clusterer
	 * 
	 */
	private static void runClusterer() {
		clusterer = new QChtc();
		// stub method
	}
	
	/**
	 * Sample - Takes large files and outputs the first n lines to another file
	 *  @param inputDir
	 */
	private static void sampleFiles(String inputDir) {
		sampler = new BigFileSampler(inputDir);
		sampler.run();
	}
	
	/**
	 * Files:
	 * 
	 * yagoDBPediaClasses:		{ wikicategories | wordnet ids }	--- owl:equivalentClass -->		{ dbpedia href http:// }
	 * yagoDBPediaInstances:	{ dbpedia entities }				--> owl:sameAs -->				{ dbpedia href http:// }
	 * yagoFacts:				{ yagoFactId , yagoEntity }			--> yagoRelation -->			{ yagoEntity }
	 * yagoImportantTypes:		{ yago ???
	 * 
	 * Right now all output is focused on entity -- Files are about entities and their mappings to everything else -- dbpedia classes etc.
	 * If we need classes mapping to entities then change each processor to have multiple file writers.
	 * 
	 */
	private static void processYago() {
		yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				new YagoTypesProcessor("input/yago/tsv/yagoSimpleTypes.tsv", "tsv", "output/yago-out/entities", "tsv", "yagoSimpleTypes-dump"),
				new YagoTypesProcessor("input/yago/tsv/yagoImportantTypes.tsv", "tsv", "output/yago-out/entities", "tsv", "yagoImportantTypes-dump"),
				new YagoTypesProcessor("input/yago/tsv/yagoTransitiveType.tsv", "tsv", "output/yago-out/entities", "tsv", "yagoTransitiveType-dump")
		});
		yagoProcessor.run();
	}
	
	private static void regexTest() {
		System.out.println(Pattern.matches("([\\t][<]([\\S]*)[>][\\t][\\S]*[\\t][<][\\S]*[>][\\t])?",
				"\t<communist_Czechoslovakia>\trdf:type\t<wordnet_communist_economy_108367579>\t"));
	}
	
	
	/** */
	public static void main(String[] args) {
		
		//regexTest();
		
		//preprocessQueryLogs();
		//sampleFiles("input/yago/tsv");
		
		// REMEMBER TO DELETE PREVIOUS OUTPUT FILES before running anything below this line!!
		processYago();		
		
	}
	
}
