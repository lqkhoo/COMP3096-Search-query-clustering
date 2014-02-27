import model.YagoHierarchy;
import processor.Preprocessor;
import processor.QueryMapper;
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
import writer.MongoWriter;

public class Main {
	
	private static Preprocessor preprocessor;
	private static BigFileSampler sampler;
	private static YagoProcessor yagoProcessor;
	private static MongoWriter mongoWriter;
	private static QueryMapper queryMapper;
	
	/** Preprocessor - Take logs and output segmented JSON search session objects */
	private static void preprocessQueryLogs() {
		preprocessor = new Preprocessor();
		preprocessor.run();
	}
	
	/** Sample - Takes large files and outputs the first n lines to another file */
	private static void sampleFiles(String inputDir) {
		sampler = new BigFileSampler(inputDir);
		sampler.run();
	}
	
	private static void getYagoEntities() {
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		yagoProcessor = new YagoProcessor(new AYagoProcessor[] {
				
				// Tests --
				
				//new YagoSimpleTypesProcessor(		mongoWriter, "output/sampler-out/yagoSimpleTypes.tsv", "tsv"),
				
				
				// Already in database -- 
				
				//new YagoSimpleTypesProcessor(		mongoWriter, "input/yago/tsv/yagoSimpleTypes.tsv", "tsv"),
				//new YagoImportantTypesProcessor(	mongoWriter, "input/yago/tsv/yagoImportantTypes.tsv", "tsv"),
				//new YagoTransitiveTypesProcessor(	mongoWriter, "input/yago/tsv/yagoTransitiveType.tsv", "tsv"),
				//new YagoTypesProcessor(			mongoWriter, "input/yago/tsv/yagoTypes.tsv", "tsv"),
				//new YagoWikipediaInfoProcessor(	mongoWriter, "input/yago/tsv/yagoWikipediaInfo.tsv", "tsv")
				//new YagoWordnetDomainsProcessor(	mongoWriter, "input/yago/tsv/yagoWordnetDomains.tsv", "tsv")
				
				// Not run --
				
				//new YagoLabelsProcessor( 			mongoWriter, "input/yago/tsv/yagoLabels.tsv", "tsv")
				
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
				// new YagoSimpleTaxonomyProcessor(	hierarchy, "input/yago/tsv/yagoSimpleTaxonomy.tsv", "tsv"),
				// new YagoTaxonomyProcessor(			hierarchy, "input/yago/tsv/yagoTaxonomy.tsv", "tsv"),
				
				
		});
		yagoProcessor.run();
		hierarchy.toFile();
	}
	
	private static void mapQueries() {
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		queryMapper = new QueryMapper(mongoWriter);
		queryMapper.run();
	}
	
	// Removes entities with just cleanName and null name fields which I have no idea how they got into the database
	/*
	private static void removeNullNamedEntities() {
		int updateCount = 0;
		
		DBCollection entities;
		DBCursor cursor;
		DBObject entity;
		
		String name;
		
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		
		entities = mongoWriter.getEntities();
		cursor = entities.find(new BasicDBObject());
		while(cursor.hasNext()) {
			entity = cursor.next();
			name = (String) entity.get("name");
			if(name == null) {
				entities.remove(entity);
				updateCount++;
				if(updateCount % 1000 == 0) {
					System.out.println("Removed " + updateCount / 1000 + "k entities.");
				}
			}
			
		}
		System.out.println("Update operation complete");
		
		mongoWriter.close();
	}
	*/
	
	// Removes trailing space in about 300k cleanName attributes
	/*
	private static void fixEntitiesCleanNameWhitespace() {
		
		int updateCount = 0;
		
		DBCollection entities;
		DBCursor cursor;
		DBObject entity;
		
		Pattern pattern = Pattern.compile("(^(.*)[ ]$)");
		Matcher matcher;
		
		String name;
		String cleanName;
		
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		
		entities = mongoWriter.getEntities();
		cursor = entities.find(new BasicDBObject());
		while(cursor.hasNext()) {
			entity = cursor.next();
			name = (String) entity.get("name");
			cleanName = (String) entity.get("cleanName");
			matcher = pattern.matcher(cleanName);
			if(matcher.find()) {
				cleanName = matcher.group(2);
				entities.update(new BasicDBObject("name", name), new BasicDBObject("cleanName", cleanName), false, false);
				updateCount++;
				if(updateCount % 1000 == 0) {
					System.out.println("Updated " + updateCount / 1000 + "k entities.");
				}
			}
			
		}
		System.out.println("Update operation complete");
		
		mongoWriter.close();
	}
	*/
	
	private static void printEntities() {
		
		DBCollection entities;
		DBCursor cursor;
		DBObject entity;
		
		String name;
		String cleanName;
		
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		
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
	
	private static void printEntityMappings() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		
		DBCollection collection;
		DBCursor cursor;
		DBObject entityMap;
		
		String name;
		String cleanName;
		
		mongoWriter = new MongoWriter("localhost", 27017, "yago2");
		
		
		collection = mongoWriter.getEntityMappingsCollection();
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
		
		// preprocessQueryLogs();
		// sampleFiles("input/yago/tsv");
		
		// getYagoEntities();
		// getYagoHierarchy();
		mapQueries();
		
		// mongoDBQueryPerformanceTest();
		// printEntities();
		// printEntityMappings();
		
		// REMEMBER TO DELETE PREVIOUS OUTPUT FILES before running anything below this line!!

		
	}
	
}
