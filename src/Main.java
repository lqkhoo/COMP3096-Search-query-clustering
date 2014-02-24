import model.YagoHierarchy;
import processor.IQueryClusterer;
import processor.Preprocessor;
import processor.QChtc;
import processor.YagoProcessor;
import processor.yago.AYagoProcessor;
import processor.yago.YagoImportantTypesProcessor;
import processor.yago.YagoSimpleTaxonomyProcessor;
import processor.yago.YagoSimpleTypesProcessor;
import processor.yago.YagoTaxonomyProcessor;
import processor.yago.YagoTransitiveTypesProcessor;
import processor.yago.YagoTypesProcessor;
import processor.yago.YagoWikipediaInfoProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reader.BigFileSampler;
import writer.MongoWriter;

public class Main {
	
	private static Preprocessor preprocessor;
	private static IQueryClusterer clusterer;
	private static BigFileSampler sampler;
	private static YagoProcessor yagoProcessor;
	private static MongoWriter mongoWriter;
	
	/** Preprocessor - Take logs and output segmented JSON search session objects */
	private static void preprocessQueryLogs() {
		preprocessor = new Preprocessor();
		preprocessor.run();
	}
	
	/** Query clusterer */
	private static void runClusterer() {
		clusterer = new QChtc();
		// stub method
	}
	
	/** Sample - Takes large files and outputs the first n lines to another file */
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
	
	private static void getYagoEntities() {
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				
				// Already in database -- 
				
				//new YagoSimpleTypesProcessor(		mongoWriter, "input/yago/tsv/yagoSimpleTypes.tsv", "tsv"),
				//new YagoImportantTypesProcessor(	mongoWriter, "input/yago/tsv/yagoImportantTypes.tsv", "tsv"),
				//new YagoTransitiveTypesProcessor(	mongoWriter, "input/yago/tsv/yagoTransitiveType.tsv", "tsv"),
				//new YagoTypesProcessor(			mongoWriter, "input/yago/tsv/yagoTypes.tsv", "tsv"),
				//new YagoWikipediaInfoProcessor(	mongoWriter, "input/yago/tsv/yagoWikipediaInfo.tsv", "tsv")
				
		});
		yagoProcessor.run();
		mongoWriter.close();
	}
	
	private static void getYagoHierarchy() {
		YagoHierarchy hierarchy = new YagoHierarchy();
		
		yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				
				// Tests --

				//new YagoSimpleTypesProcessor(mongoWriter, "output/sampler-out/yagoSimpleTypes.tsv", "tsv"),
				//new YagoTypesProcessor(mongoWriter, "output/sampler-out/yagoTypes.tsv", "tsv")
				//new YagoWikipediaInfoProcessor(mongoWriter, "output/sampler-out/yagoWikipediaInfo.tsv", "tsv")
				
				// Hierarchy operations --
				new YagoSimpleTaxonomyProcessor(	hierarchy, "input/yago/tsv/yagoSimpleTaxonomy.tsv", "tsv"),
				new YagoTaxonomyProcessor(			hierarchy, "input/yago/tsv/yagoTaxonomy.tsv", "tsv"),
				
				
		});
		yagoProcessor.run();
		hierarchy.toFile();
	}
	
	private static void mongoDBQueryPerformanceTest() {
		
		long startTime = System.currentTimeMillis();
		long endTime;
		int timeTaken;
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		System.out.println("Main: Running MongoDB query performance test.");
		
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		
		String[] entities = new String[] {
			"Walter Oi",
			"Elizabeth Fretwell",
			"Miquel Nelom",
			"Ali bin Ali Douha",
			"Keio University",
			"Southland Astronomical Society Observatory",
			"Zebrilus",
			"Shannon Lively",
			"Slobodanka Stupar",
			"Moss Hart"
		};
		
		String[] classes = new String[] {
			
		};
		
		for(String entity : entities) {
			System.out.println(gson.toJson(mongoWriter.getEntity(entity)));
		}
		
		// classes
		
		endTime = System.currentTimeMillis();
		timeTaken = (int) ((endTime - startTime) / 1000);
		System.out.println("Main: MongoDB performance test: " + timeTaken + " seconds, queried " + entities.length + " entities, " + classes.length + " classes.");
		mongoWriter.close();
	}
	
	/** */
	public static void main(String[] args) {
		
		preprocessQueryLogs();
		//sampleFiles("input/yago/tsv");
		
		//getYagoHierarchy();
		//mongoDBQueryPerformanceTest();
		//System.out.println(mongoWriter.getEntityCount());
		
		// REMEMBER TO DELETE PREVIOUS OUTPUT FILES before running anything below this line!!

		
	}
	
}
