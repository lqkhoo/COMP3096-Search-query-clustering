import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import model.SessionToClassMapping;
import model.YagoHierarchy;
import processor.DBCacher;
import processor.EntityClusterer;
import processor.Preprocessor;
import processor.QueryMapper;
import processor.SessionClusterer;
import processor.YagoProcessor;
import processor.yago.AYagoProcessor;
import processor.yago.YagoImportantTypesProcessor;
import processor.yago.YagoLabelsProcessor;
import processor.yago.YagoSimpleTaxonomyProcessor;
import processor.yago.YagoSimpleTypesProcessor;
import processor.yago.YagoTaxonomyProcessor;
import processor.yago.YagoTransitiveTypesProcessor;
import processor.yago.YagoTypesProcessor;
import processor.yago.YagoWikipediaInfoProcessor;
import processor.yago.YagoWordnetDomainsProcessor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import reader.BigFileSampler;
import reader.PreprocessedLogReader;
import writer.MongoWriter;

public class Main {
	
	public static final String MONGODB_HOST = "localhost";
	public static final int MONGODB_PORT = 27017;
	public static final String MONGODB_DBNAME = "yago2";
	
	
	// Helper classes
	private static MongoWriter newMongoWriter() {
		MongoWriter mongoWriter = new MongoWriter(MONGODB_HOST, MONGODB_PORT, MONGODB_DBNAME);
		return mongoWriter;
	}
	
	// Operator classes
	
	/**
	 * Preprocessor - Reads in AOL search logs and outputs segmented JSON search session objects.
	 * Currently output is written to file.
	 * 
	 * Expected runtime: several minutes (Core i7 2GHz)
	 */
	private static void preprocessQueryLogs() {
		Preprocessor preprocessor = new Preprocessor();
		preprocessor.run();
	}
	
	/**
	 * Sample - Takes large files and outputs the first n lines to another file
	 * This is a utility class to allow easy inspection of file formats of huge files and so on.
	 */
	private static void sampleFiles(String inputDir) {
		BigFileSampler sampler = new BigFileSampler(inputDir);
		sampler.run();
	}
	
	/**
	 * This reads in Yago files containing entity information
	 * and outputs them into the MongoDB collection "entities"
	 * 
	 * Expected runtime: Several minutes (Core i7 2GHz)
	 */
	private static void getYagoEntities() {
		MongoWriter mongoWriter = newMongoWriter();
		YagoProcessor yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				
				// -- Tests --
				//new YagoSimpleTypesProcessor(		mongoWriter, "output/sampler-out/yagoSimpleTypes.tsv", "tsv"),
				
				// -- Already in database -- 
				//new YagoSimpleTypesProcessor(		mongoWriter, "input/yago/tsv/yagoSimpleTypes.tsv", "tsv"),
				//new YagoImportantTypesProcessor(	mongoWriter, "input/yago/tsv/yagoImportantTypes.tsv", "tsv"),
				//new YagoTransitiveTypesProcessor(	mongoWriter, "input/yago/tsv/yagoTransitiveType.tsv", "tsv"),
				//new YagoTypesProcessor(			mongoWriter, "input/yago/tsv/yagoTypes.tsv", "tsv"),
				//new YagoWikipediaInfoProcessor(	mongoWriter, "input/yago/tsv/yagoWikipediaInfo.tsv", "tsv")
				//new YagoWordnetDomainsProcessor(	mongoWriter, "input/yago/tsv/yagoWordnetDomains.tsv", "tsv")
				
				// -- Not run (output not currently deemed useful / necessary) --
				//new YagoLabelsProcessor( 			mongoWriter, "input/yago/tsv/yagoLabels.tsv", "tsv")
				
		});
		yagoProcessor.run();
		mongoWriter.close();
	}
	
	/**
	 * This reads in Yago files containing class hierarchy information
	 * and either holds them in a hashmap, writes them to file, or 
	 * outputs them into the MongoDB collection "classes"
	 * 
	 * Runtime: Several seconds to generate hashmap, writing to db takes 1-2 minutes (i7 2GHz)
	 */
	private static void getYagoHierarchy() {
		MongoWriter mongoWriter = newMongoWriter();
		YagoHierarchy hierarchy = new YagoHierarchy(mongoWriter);
		YagoProcessor yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				
				// -- Tests --
				
				// -- Hierarchy operations --
				// new YagoSimpleTaxonomyProcessor(	hierarchy, "input/yago/tsv/yagoSimpleTaxonomy.tsv", "tsv"),
				// new YagoTaxonomyProcessor(			hierarchy, "input/yago/tsv/yagoTaxonomy.tsv", "tsv"),
				
				
		});
		
		yagoProcessor.run();
		// hierarchy.toFile();	// Uncomment this line to write to file
		hierarchy.toDb(); 		// Uncomment this line to write to MongoDB
	}
	
	/**
	 * 
	 */
	private static void augmentClassesWithNId() {
		MongoWriter mongoWriter = newMongoWriter();
		DBCollection classes = mongoWriter.getClassesCollection();
		DBCursor cursor;
		DBObject cls;
		
		String className;
		int nId = 0;
		
		int classesProcessed = 0;
		
		cursor = classes.find(new BasicDBObject());
		while(cursor.hasNext()) {
			cls = cursor.next();
			className = (String) cls.get("name");
			mongoWriter.setClassNId(className, nId);
			
			nId++;
			classesProcessed++;
			if(classesProcessed % 1000 == 0) {
				System.out.println("augmentClassesWithNId: Classes processed: " + classesProcessed / 1000 + "k entities.");
			}
		}
	}
	
	/**
	 * 
	 * 
	 */
	private static void mapYagoHierarchyToEntities() {
		MongoWriter mongoWriter = newMongoWriter();
		EntityClusterer entityClusterer = new EntityClusterer(mongoWriter);
		YagoSimpleTypesProcessor processor = new YagoSimpleTypesProcessor(mongoWriter, "input/yago/tsv/yagoSimpleTypes.tsv", "tsv");
		
		// either only map leaves or all categories but never do both
		
		// entityClusterer.mapAll();
		entityClusterer.mapLeaves(processor);
	}
	
	/**
	 * This reads in the output of the Preprocessor class (time-segmented AOL log files in JSON format)
	 * and performs an n x n mapping of query string to query string within each session where each query string
	 *   must have a mapping to the "searchString" attribute of an entity in the "entities" MongoDB collection,
	 *   and then it writes the information to the "searchMap" collection in MongoDB
	 *   
	 * Expected runtime: 24 - 36 hours (Core i7 2GHz)
	 */
	@Deprecated
	private static void mapQueries() {
		MongoWriter mongoWriter = newMongoWriter();
		QueryMapper queryMapper = new QueryMapper(mongoWriter);
		queryMapper.run();
	}
	
	private static void cacheValidSearchStrings() {
		MongoWriter mongoWriter = newMongoWriter();
		DBCacher dbCacher = new DBCacher(mongoWriter);
		dbCacher.cacheValidEntitySearchStrings();
	}
	
	private static void cacheSearchStringsToClasses() {
		MongoWriter mongoWriter = newMongoWriter();
		DBCacher dbCacher = new DBCacher(mongoWriter);
		dbCacher.cacheSearchStringsToClasses();
	}
	
	/**
	 * This clusters search sessions
	 * 
	 * Currently takes the output of mapQueries() as input to avoid re-matching the substrings
	 * from query logs to mongoDB entity entries, which is very computationally and I/O-expensive
	 */
	private static void findUsefulSearchSessions() {
		
		MongoWriter mongoWriter = newMongoWriter();
		SessionClusterer sessionClusterer = new SessionClusterer(mongoWriter);
		sessionClusterer.findUsefulSessions();
	}
	
	private static void clusterSearchSessions() {
		MongoWriter mongoWriter = newMongoWriter();
		SessionClusterer sessionClusterer = new SessionClusterer(mongoWriter);
		sessionClusterer.clusterSessions();
	}
	
	private static void printEntities() {
		
		DBCollection entities;
		DBCursor cursor;
		DBObject entity;
		
		String name;
		String cleanName;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		entities = mongoWriter.getEntitiesCollection();
		cursor = entities.find(new BasicDBObject());
		
		System.out.println(entities.count());
		
		try {
			while(cursor.hasNext()) {
				entity = cursor.next();
				name = (String) entity.get("name");
				cleanName = (String) entity.get("cleanName");
			}
		} finally {
			mongoWriter.close();
		}

	}
	
	private static void printEntities(String searchString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		DBCollection collection;
		DBCursor cursor;
		DBObject entityMap;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		collection = mongoWriter.getEntitiesCollection();
		cursor = collection.find(new BasicDBObject("searchString", searchString));
		
		try {
			while(cursor.hasNext()) {
				entityMap = cursor.next();
				System.out.println(gson.toJson(entityMap));
			}
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printSearchMap(String searchString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		DBCollection collection;
		DBCursor cursor;
		DBObject entityMap;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		
		collection = mongoWriter.getSearchMapsCollection();
		cursor = collection.find(new BasicDBObject("searchString", searchString));
		
		try {
			while(cursor.hasNext()) {
				entityMap = cursor.next();
				System.out.println(gson.toJson(entityMap));
			}
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printSearchMaps() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		DBCollection collection;
		DBCursor cursor;
		DBObject entityMap;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		collection = mongoWriter.getSearchMapsCollection();
		cursor = collection.find(new BasicDBObject());
		
		System.out.println(collection.count());
		
		try {
			while(cursor.hasNext()) {
				entityMap = cursor.next();
				System.out.println(gson.toJson(entityMap));
			}
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printClass(String name) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		MongoWriter mongoWriter = newMongoWriter();
		try {
			System.out.println(gson.toJson(mongoWriter.getClass(name)));
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printClasses() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		DBCollection classes;
		DBCursor cursor;
		DBObject yagoClass;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		classes = mongoWriter.getClassesCollection();
		cursor = classes.find(new BasicDBObject());
		
		System.out.println(classes.count());
		
		try {
			while(cursor.hasNext()) {
				yagoClass = cursor.next();
				System.out.println(gson.toJson(yagoClass));
			}
		} finally {
			mongoWriter.close();
		}
	}

	private static void printClassMembers(String name) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		MongoWriter mongoWriter = newMongoWriter();
		try {
			System.out.println(gson.toJson(mongoWriter.getClassMembers(name)));
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void mongoDBQueryPerformanceTest() {
		
		long startTime = System.currentTimeMillis();
		long endTime;
		int timeTaken;
				
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		MongoWriter mongoWriter = newMongoWriter();
		
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
		
		System.out.println("Main: Running MongoDB query performance test.");
		for(String entity : entities) {
			System.out.println(gson.toJson(mongoWriter.getEntities(new BasicDBObject("cleanName", entity))));
		}
		
		// classes
		
		endTime = System.currentTimeMillis();
		timeTaken = (int) ((endTime - startTime) / 1000);
		System.out.println("Main: MongoDB performance test: " + timeTaken + " seconds, queried " + entities.length + " entities, " + classes.length + " classes.");
		mongoWriter.close();
	}
	
	/** */
	public static void main(String[] args) {
				
		/* Data inspection methods */
		// mongoDBQueryPerformanceTest();
		// printEntities();
		// printEntities("scala");
		// printClasses();
		// printClass("<wordnet_bishop_109857200>");
		// printClassMembers("<wordnet_bishop_109857200>");
		// printSearchMaps();
		// printSearchMap("indonesia");
		
		/* Operator calls */
		// preprocessQueryLogs();
		// getYagoEntities();
		// getYagoHierarchy();
		// augmentClassesWithNId();
		// mapYagoHierarchyToEntities();
		
		// mapQueries(); // Deprecated
		// cacheValidSearchStrings();
		// cacheSearchStringsToClasses();
		findUsefulSearchSessions();
		// clusterSearchSessions();
		
		/* Utility methods */
		// sampleFiles("input/yago/tsv");
		
	}
	
}
