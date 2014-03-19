import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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

/**
 * 
 * @author Li Quan Khoo
 */
public class Main {
	
	public static final String MONGODB_HOST = "localhost";
	public static final int MONGODB_PORT = 27017;
	public static final String MONGODB_DBNAME = "yago2";
	
	// Helper classes
	private static MongoWriter newMongoWriter() {
		MongoWriter mongoWriter = new MongoWriter(MONGODB_HOST, MONGODB_PORT,
				MONGODB_DBNAME);
		return mongoWriter;
	}
	
	// Operator classes
	
	/**
	 * Preprocessor - Reads in AOL search logs and outputs segmented JSON search
	 * session objects. Currently output is written to file.
	 * 
	 * Expected runtime: several minutes (Core i7 2GHz)
	 */
	private static void preprocessQueryLogs() {
		Preprocessor preprocessor = new Preprocessor();
		preprocessor.run();
	}
	
	/**
	 * This reads in Yago files containing entity information and outputs them
	 * into the MongoDB collection "entities"
	 * 
	 * Expected runtime: Several minutes (Core i7 2GHz)
	 */
	private static void getYagoEntities() {
		MongoWriter mongoWriter = newMongoWriter();
		YagoProcessor yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
			
			// -- Tests --
			// new YagoSimpleTypesProcessor( 		mongoWriter, "output/sampler-out/yagoSimpleTypes.tsv", "tsv"),
			
			// -- Already in database --
			// new YagoSimpleTypesProcessor(		mongoWriter, "input/yago/tsv/yagoSimpleTypes.tsv", "tsv"),
			// new YagoImportantTypesProcessor(		mongoWriter, "input/yago/tsv/yagoImportantTypes.tsv", "tsv"),
			// new YagoTransitiveTypesProcessor(	mongoWriter, "input/yago/tsv/yagoTransitiveType.tsv", "tsv"),
			// new YagoTypesProcessor(				mongoWriter, "input/yago/tsv/yagoTypes.tsv", "tsv"),
			// new YagoWikipediaInfoProcessor(		mongoWriter, "input/yago/tsv/yagoWikipediaInfo.tsv", "tsv")
			// new YagoWordnetDomainsProcessor(		mongoWriter, "input/yago/tsv/yagoWordnetDomains.tsv", "tsv")
			
			// -- Not run (output not currently deemed useful / necessary)
			// --
			// new YagoLabelsProcessor(				mongoWriter, "input/yago/tsv/yagoLabels.tsv", "tsv")
			
			});
		yagoProcessor.run();
		mongoWriter.close();
	}
	
	/**
	 * This reads in Yago files containing class hierarchy information and
	 * either holds them in a hashmap, writes them to file, or outputs them into
	 * the MongoDB collection "classes"
	 * 
	 * Runtime: Several seconds to generate hashmap, writing to db takes 1-2
	 * minutes (i7 2GHz)
	 */
	private static void getYagoHierarchy() {
		MongoWriter mongoWriter = newMongoWriter();
		YagoHierarchy hierarchy = new YagoHierarchy(mongoWriter);
		YagoProcessor yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				
		// -- Tests --
				
				// -- Hierarchy operations --
				// new YagoSimpleTaxonomyProcessor(	hierarchy, "input/yago/tsv/yagoSimpleTaxonomy.tsv", "tsv"),
				// new YagoTaxonomyProcessor(		hierarchy, "input/yago/tsv/yagoTaxonomy.tsv", "tsv"),
				
				});
		
		yagoProcessor.run();
		// hierarchy.toFile(); // Uncomment this line to write to file
		hierarchy.toDBClassesCollection(); // Uncomment this line to write to MongoDB
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
		while (cursor.hasNext()) {
			cls = cursor.next();
			className = (String) cls.get("name");
			mongoWriter.setClassNId(className, nId);
			
			nId++;
			classesProcessed++;
			if (classesProcessed % 1000 == 0) {
				System.out.println("augmentClassesWithNId: Classes processed: "
						+ classesProcessed / 1000 + "k entities.");
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
		YagoSimpleTypesProcessor processor = new YagoSimpleTypesProcessor(
				mongoWriter, "input/yago/tsv/yagoSimpleTypes.tsv", "tsv");
		
		// either only map leaves or all categories but never do both
		
		// entityClusterer.mapAll();
		entityClusterer.mapLeaves(processor);
	}
	
	/**
	 * This reads in the output of the Preprocessor class (time-segmented AOL
	 * log files in JSON format) and performs an n x n mapping of query string
	 * to query string within each session where each query string must have a
	 * mapping to the "searchString" attribute of an entity in the "entities"
	 * MongoDB collection, and then it writes the information to the "searchMap"
	 * collection in MongoDB
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
	 * Finds sessions with more than one valid searchString and outputs them to
	 * the usefulSessions collection Runtime: ~ 30 minutes
	 */
	private static void findUsefulSearchSessions() {

		MongoWriter mongoWriter = newMongoWriter();
		SessionClusterer sessionClusterer = new SessionClusterer(mongoWriter);
		sessionClusterer.findUsefulSessions();
	}
	
	/**
	 * Finds semantic similarity between entities in each usefulSession and
	 * outputs to semanticSessions collection Runtime: ~ minutes to ~ 2 hours,
	 * depending on similarityThreshold. I/O bound.
	 * 
	 * @param similarityThreshold
	 */
	private static void findSessionSemantics(double similarityThreshold) {
		MongoWriter mongoWriter = newMongoWriter();
		SessionClusterer sessionClusterer = new SessionClusterer(mongoWriter);
		sessionClusterer.deriveSessionSemantics(similarityThreshold);
	}
	
	private static void clusterSessions() {
		MongoWriter mongoWriter = newMongoWriter();
		SessionClusterer sessionClusterer = new SessionClusterer(mongoWriter);
		sessionClusterer.clusterSessions();
	}
	
	private static void constructClassToEntityMappings() {
		MongoWriter mongoWriter = newMongoWriter();
		SessionClusterer sessionClusterer = new SessionClusterer(mongoWriter);
		sessionClusterer.constructClassToEntityMappings();
	}
	
	private static void constructEntityToEntityMappings() {
		MongoWriter mongoWriter = newMongoWriter();
		SessionClusterer sessionClusterer = new SessionClusterer(mongoWriter);
		sessionClusterer.constructEntityToEntityMappings();
	}
	
	// Data inspection methods
	
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
			while (cursor.hasNext()) {
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
		cursor = collection
				.find(new BasicDBObject("searchString", searchString));
		
		try {
			while (cursor.hasNext()) {
				entityMap = cursor.next();
				System.out.println(gson.toJson(entityMap));
			}
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printSearchMap(String searchString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		
		DBCollection collection;
		DBCursor cursor;
		DBObject entityMap;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		collection = mongoWriter.getSearchMapsCollection();
		cursor = collection
				.find(new BasicDBObject("searchString", searchString));
		
		try {
			while (cursor.hasNext()) {
				entityMap = cursor.next();
				System.out.println(gson.toJson(entityMap));
			}
		} finally {
			mongoWriter.close();
		}
	}

	private static void printSearchMaps() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		
		DBCollection collection;
		DBCursor cursor;
		DBObject entityMap;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		collection = mongoWriter.getSearchMapsCollection();
		cursor = collection.find(new BasicDBObject());
		
		System.out.println(collection.count());
		
		try {
			while (cursor.hasNext()) {
				entityMap = cursor.next();
				System.out.println(gson.toJson(entityMap));
			}
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printClass(String name) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		
		MongoWriter mongoWriter = newMongoWriter();
		try {
			System.out.println(gson.toJson(mongoWriter.getClass(name)));
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printClasses() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		
		DBCollection classes;
		DBCursor cursor;
		DBObject yagoClass;
		
		MongoWriter mongoWriter = newMongoWriter();
		
		classes = mongoWriter.getClassesCollection();
		cursor = classes.find(new BasicDBObject());
		
		System.out.println(classes.count());
		
		try {
			while (cursor.hasNext()) {
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
	
	private static void printSemanticSession(int sessionId) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		MongoWriter mongoWriter = newMongoWriter();
		try {
			System.out.println(gson.toJson(mongoWriter.getSemanticSessionsCollection()
					.findOne(new BasicDBObject("sessionId", sessionId))));
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printSessionCluster(String entityName) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		MongoWriter mongoWriter = newMongoWriter();
		try {
			System.out.println(gson.toJson(mongoWriter.getSessionClustersCollection()
					.findOne(new BasicDBObject("entityName", entityName))));
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void printClusterClassToEntityMapping() {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		MongoWriter mongoWriter = newMongoWriter();
		try {
			
			DBCursor cursor = mongoWriter.getClusterMappingsClassToEntityCollection().find(new BasicDBObject());
			DBObject obj;
			while(cursor.hasNext()) {
				obj = cursor.next();
				System.out.println(gson.toJson(obj));
			}
		} finally {
			mongoWriter.close();
		}
	}
	
	private static void mongoDBQueryPerformanceTest() {
		
		long startTime = System.currentTimeMillis();
		long endTime;
		int timeTaken;
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		MongoWriter mongoWriter = newMongoWriter();
		
		String[] entities = new String[] { "Walter Oi", "Elizabeth Fretwell",
				"Miquel Nelom", "Ali bin Ali Douha", "Keio University",
				"Southland Astronomical Society Observatory", "Zebrilus",
				"Shannon Lively", "Slobodanka Stupar", "Moss Hart" };
		
		String[] classes = new String[] {
				
		};

		System.out.println("Main: Running MongoDB query performance test.");
		for (String entity : entities) {
			System.out.println(gson.toJson(mongoWriter
					.getEntities(new BasicDBObject("cleanName", entity))));
		}
		
		// classes
		
		endTime = System.currentTimeMillis();
		timeTaken = (int) ((endTime - startTime) / 1000);
		System.out.println("Main: MongoDB performance test: " + timeTaken
				+ " seconds, queried " + entities.length + " entities, "
				+ classes.length + " classes.");
		mongoWriter.close();
	}
	
	
	
	// Utility classes
	/**
	 * Sample - Takes large files and outputs the first n lines to another file
	 * This is a utility class to allow easy inspection of file formats of huge
	 * files and so on.
	 */
	private static void sampleFiles(String inputDir) {
		BigFileSampler sampler = new BigFileSampler(inputDir);
		sampler.run();
	}
	
	private static void printAllLogQueriesCount() {
		
		int lines = 0;
		String[] fileNames = new String[]{
				"input/querylogs/user-ct-test-collection-01.txt",
				"input/querylogs/user-ct-test-collection-02.txt",
				"input/querylogs/user-ct-test-collection-03.txt",
				"input/querylogs/user-ct-test-collection-04.txt",
				"input/querylogs/user-ct-test-collection-05.txt",
				"input/querylogs/user-ct-test-collection-06.txt",
				"input/querylogs/user-ct-test-collection-07.txt",
				"input/querylogs/user-ct-test-collection-08.txt",
				"input/querylogs/user-ct-test-collection-09.txt",
				"input/querylogs/user-ct-test-collection-10.txt"
		};
		
		for(int i = 0; i <= fileNames.length; i++) {
			try {
				lines += countLinesInFile(fileNames[i]);
			} catch (Exception e) {}
		}
		
		lines -= fileNames.length; // First line in each file is not data
		System.out.println(lines);
	}
	
	// Fully copied from
	// http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	private static int countLinesInFile(String inputDir) throws IOException {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(inputDir));
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}
	
	/** */
	public static void main(String[] args) {
		
		/* Data inspection methods */
		// mongoDBQueryPerformanceTest();
		// printEntities();
		// printEntities("scala");
		// printClasses();
		// printClass("<wordnet_bishop_109857200>");
		// printClass("owl:Thing");
		// printClass("<wordnet_organization_108008335>");
		// printClassMembers("<wordnet_bishop_109857200>");
		// printSearchMaps();
		// printSearchMap("indonesia");
		// printSessionCluster("<Michigan>");
		// printSemanticSession(8001449);
		// printClusterClassToEntityMapping();
		
		/* Operator calls */
		// preprocessQueryLogs();
		// getYagoEntities();
		// getYagoHierarchy();
		// augmentClassesWithNId();
		// mapYagoHierarchyToEntities();
		
		// mapQueries(); // Deprecated
		// cacheValidSearchStrings();
		// cacheSearchStringsToClasses();
		// findUsefulSearchSessions();
		// findSessionSemantics(8); // Similarity threshold of 8
		// clusterSessions();
		// constructClassToEntityMappings();
		// constructEntityToEntityMappings();
		
		/* Utility methods */
		// sampleFiles("input/yago/tsv");
		// printAllLogQueriesCount();
		
	}
	
}
