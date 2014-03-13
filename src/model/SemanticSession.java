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
	public final double similarityThreshold;	// The minimum similarity between similarities for them to be recorded into the DB.
												// If no pair of entities have similarities >= similarityThreshold, then session is not recorded into DB.
	private String[] searchStrings;				// valid searchStrings found within this session
	private String[] searchStringsUnqualified;	// searchStrings of which none of the entities >= similarityThreshold
	
	private transient HashMap<String, ArrayList<SemanticEntity>> entities;
	private ArrayList<String> entityNames;
	private ArrayList<Similarity> similarities;
	//private Similarity[][] similarityMatrix;	// matrix representation not necessary and complicates serialization
	
	public SemanticSession(int sessionId, String[] searchStrings, double similarityThreshold, MongoWriter mongoWriter) {
		this.mongoWriter = mongoWriter;
		
		this.sessionId = sessionId;
		this.similarityThreshold = similarityThreshold;
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
		
		HashSet<String> unqualifiedSearchStrings;
		
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
					break;	// handshake -- compare each pair only once
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
						similarity = new Similarity(entityX.entityName, searchStrings[i],
								entityY.entityName, searchStrings[j], commonClasses, commonLinks);
						// this.similarityMatrix[x][y] = similarity;
						// this.similarityMatrix[y][x] = similarity;
						this.similarities.add(similarity);
					}
				}
				
			}
		}
		Collections.sort(this.similarities);
		
		unqualifiedSearchStrings = new HashSet<String>(Arrays.asList(this.searchStrings));
		for(int i = 0; i < this.similarities.size(); i++) {
			similarity = similarities.get(i);
			
			if(similarity.similarity < this.similarityThreshold) {
				break;
			}
			
			unqualifiedSearchStrings.remove(similarity.entity1SearchString);
			unqualifiedSearchStrings.remove(similarity.entity2SearchString);
		}
		this.searchStringsUnqualified = unqualifiedSearchStrings.toArray(new String[]{});
		
	}
	
	public String[] getSearchStrings() {
		return this.searchStrings;
	}
	
	public String[] getUnqualifiedSearchStrings() {
		return this.searchStringsUnqualified;
	}
	
	public String[] getEntityNames() {
		return this.entityNames.toArray(new String[]{});
	}
	
	public Similarity[] getSimilarities() {
		return this.similarities.toArray(new Similarity[]{});
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
	

	
}
