package model;

import java.util.Set;

/**
 * 
 * Model of similarity between SemanticSession instances
 * 
 * @author Li Quan Khoo
 */
public class Similarity implements Comparable<Similarity> {
	
	public static final double COMMON_CLASS_SCORE = 1;
	public static final double COMMON_LINK_SCORE = 0.2;
	
	public final double similarity;
	public final String entity1;
	public final String entity1SearchString;
	public final String entity2;
	public final String entity2SearchString;
	public final Set<String> commonClasses;
	public final Set<String> commonLinks;
	
	public Similarity(String entity1, String entity1SearchString,
			String entity2, String entity2SearchString,
			Set<String> commonClasses, Set<String> commonLinks) {
		this.entity1 = entity1;
		this.entity1SearchString = entity1SearchString;
		this.entity2 = entity2;
		this.entity2SearchString = entity2SearchString;
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
	
}
