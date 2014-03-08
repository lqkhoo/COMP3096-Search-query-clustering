package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import writer.MongoWriter;

/**
 * Model of a search session, taking into account Yago entity information and hierarchical classes
 * 
 * @author Li Quan Khoo
 */
public class SemanticSession {
	
	private transient MongoWriter mongoWriter;
		
	public final int sessionId;
	private String[] searchStrings;	// valid searchStrings found within this session
	
	private transient HashMap<String, ArrayList<SemanticEntity>> entities;
	private ArrayList<String> entityNames;
	private ArrayList<Similarity> similarities;
	//private Similarity[][] similarityMatrix;	// matrix representation not necessary and complicates serialization
	
	public SemanticSession(int sessionId, String[] searchStrings, MongoWriter mongoWriter) {
		this.mongoWriter = mongoWriter;
		
		this.sessionId = sessionId;
		this.searchStrings = searchStrings;
		
		this.entities = new HashMap<String, ArrayList<SemanticEntity>>();
		this.entityNames = new ArrayList<String>();
		this.similarities = new ArrayList<Similarity>();
		
		initializeValues();
	}
	
	private void initializeValues() {
				
		DBCursor cursor;
		DBObject entity;
		String entityName;
		BasicDBObject relations;
		BasicDBList classes;
		BasicDBList linksTo;
		
		ArrayList<SemanticEntity> searchString1Entities;
		ArrayList<SemanticEntity> searchString2Entities;
		
		SemanticEntity entityX;
		SemanticEntity entityY;
		Similarity similarity;
		Set<String> commonClasses;
		Set<String> commonLinks;
				
		for(String searchString : this.searchStrings) {
			
			this.entities.put(searchString, new ArrayList<SemanticEntity>());
			cursor = this.mongoWriter.getEntitiesCollection().find(new BasicDBObject("searchString", searchString));
			
			while(cursor.hasNext()) {
				entity = cursor.next();
				entityName = (String) entity.get("name");
				relations = (BasicDBObject) entity.get("relations");
				if(relations != null) {
					classes = (BasicDBList) relations.get("rdf:type");
					if(classes == null) {
						classes = new BasicDBList();
					}
					linksTo = (BasicDBList) relations.get("<linksTo>");
					if(linksTo == null) {
						linksTo = new BasicDBList();
					}
					
					this.entityNames.add(entityName);
					this.entities.get(searchString).add(new SemanticEntity(entityName, searchString,
							new HashSet<String>(Arrays.asList(classes.toArray(new String[]{}))),
							new HashSet<String>(Arrays.asList(linksTo.toArray(new String[]{})))
							));
				}
			}
		}
		
		// Initialize blank similarity matrix
		// this.similarityMatrix = new Similarity[entities.size()][entities.size()];
		
		
		// For each searchString, intersect their set of entities' classes and linksTo
		//   against each other to generate a similarity score
		for(int i = 0; i < searchStrings.length; i++) {
			
			searchString1Entities = this.entities.get(searchStrings[i]);
			
			for(int j = 0; j < searchStrings.length; j++) {
				if(j >= i) {
					break;	// handshake - compare each pair only once
				}
				searchString2Entities = this.entities.get(searchStrings[j]);
				
				for(int x = 0; x < searchString1Entities.size(); x++) {
					entityX = searchString1Entities.get(x);
					
					for(int y = 0; y < searchString2Entities.size(); y++) {
						entityY = searchString2Entities.get(y);
						
						commonClasses = new HashSet<String>(entityX.classes);
						commonClasses.retainAll(entityY.classes);
						commonLinks = new HashSet<String>(entityX.linksTo);
						commonLinks.retainAll(entityY.linksTo);
						similarity = new Similarity(entityX.entityName, entityY.entityName, commonClasses, commonLinks);
						// this.similarityMatrix[x][y] = similarity;
						// this.similarityMatrix[y][x] = similarity;
						this.similarities.add(similarity);
					}
				}
				
			}
		}
		Collections.sort(this.similarities);
		
	}
	
	// Private classes
	
	private class SemanticEntity {
		
		public final String entityName;
		public final String searchString;
		public final Set<String> classes;
		public final Set<String> linksTo;
		
		public SemanticEntity(String entityName, String searchString, Set<String> classes, Set<String> linksTo) {
			this.entityName = entityName;
			this.searchString = searchString;
			this.classes = classes;
			this.linksTo = linksTo;
		}
		
	}
	
	private class Similarity implements Comparable<Similarity> {
		
		public static final double COMMON_CLASS_SCORE = 1;
		public static final double COMMON_LINK_SCORE = 0.2;
		
		public final double similarity;
		public final String entity1;
		public final String entity2;
		public final Set<String> commonClasses;
		public final Set<String> commonLinks;
		
		public Similarity(String entity1, String entity2, Set<String> commonClasses, Set<String> commonLinks) {
			this.entity1 = entity1;
			this.entity2 = entity2;
			this.commonClasses = commonClasses;
			this.commonLinks = commonLinks;
			this.similarity = commonClasses.size() * COMMON_CLASS_SCORE + commonLinks.size() * COMMON_LINK_SCORE;
		}

		@Override
		public int compareTo(Similarity other) {
			// sort in reverse order
			if(this.similarity < other.similarity) {
				return 1;
			} else if(this.similarity > other.similarity) {
				return -1;
			}
			return 0;
		}
		
		/*
		public int getSimilarity() {
			return this.similarity;
		}
		
		public int setSimilarity(int similarity) {
			this.similarity = similarity;
			return this.similarity;
		}
		
		public Set<String> getCommonClasses() {
			return this.commonClasses;
		}
		
		public Set<String> setCommonClasses(Set<String> commonClasses) {
			this.commonClasses = commonClasses;
			return this.commonClasses;
		}
		
		public Set<String> getCommonLinks() {
			return this.commonClasses;
		}
		
		public Set<String> setCommonLinks(Set<String> commonLinks) {
			this.commonLinks = commonLinks;
			return this.commonLinks;
		}
		*/
		
	}
	
}
