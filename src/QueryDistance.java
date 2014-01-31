/**
 * Class that calculates the query distance between two strings
 * 
 * @author Li Quan Khoo
 *
 */
public class QueryDistance {
	
	// Current values place all weighting on the lexical distance
	//   as we do not have the calculations for semantic distance
	
	
	// Lexical distance weighting against semantic distance
	// 0.5 means half/half, 1.0 means only consider lexical distance, 0.0 means only consider semantic distance
	// Lucchese's best-result value for using only miu(1) is 1.0 (pg284)
	public static final double DEFAULT_LEXICAL_DISTANCE_WEIGHT = 1.0;
	
	// For calculating the conditional distance function as defined in Lucchese et al. 2011
	// If lexical distance is less than this value, then semantic distance is not considered at all
	//   to satisfy the intuition that if two queries are extremely similar, then they very likely 
	//   refer to the same thing / are reformulations
	// 1.0 means always ignore semantic distance, 0.0 means always take into account semantic distance
	
	// Lucchese's value is 0.5 (pg285)
	public static final double DEFAULT_LEXICAL_DISTANCE_OVERRIDE_THRESHOLD = 0.5;
	// Lucchese's value is 4.0 (pg285)
	public static final double DEFAULT_SEMANTIC_DISTANCE_MULTIPLIER = 4.0;
	
	public QueryDistance() {
		// Nothing to initialize
	}
	
	public static double conditionalDistance(String str1, String str2) {
		
		return conditionalDistance(str1, str2,
				DEFAULT_LEXICAL_DISTANCE_OVERRIDE_THRESHOLD,
				DEFAULT_SEMANTIC_DISTANCE_MULTIPLIER);
	}
	
	public static double conditionalDistance(String str1, String str2,
			double lexicalOverrideThreshold, double semanticDistanceMultiplier) {
		
		double lexicalDistance = lexicalDistance(str1, str2);
		if(lexicalDistance < lexicalOverrideThreshold) {
			return lexicalDistance;
		} else {
			return Math.min(lexicalDistance, semanticDistanceMultiplier * semanticDistance(str1, str2));
		}
		
	}
	
	public static double distance(String str1, String str2) {
		return distance(str1, str2,	DEFAULT_LEXICAL_DISTANCE_WEIGHT);
	}
	
	public static double distance(String str1, String str2, double lexicalDistanceWeight) {
		return (lexicalDistance (str1, str2) * lexicalDistanceWeight)
				+ semanticDistance(str1, str2) * (1 - lexicalDistanceWeight);
	}
	
	/*
	 * Placeholder for content distance calculation
	 */
	public static double semanticDistance(String str1, String str2) {
		//TODO stub method
		return 0;
	}
	
	/*
	 * Calculate content distance based on normalized Levenshtein distance
	 *   and Jaccard distance calculated with tri-grams
	 */
	public static double lexicalDistance(String str1, String str2) {
		return (levenshtein(str1, str2, true) + jaccard(str1, str2, 3)) / 2;
	}
	
	/*
	 * Returns normalized Levenshtein distance between two strings
	 * O(n^2) time complexity
	 */
	public static double levenshtein(String str1, String str2) {
		return levenshtein(str1, str2, true);
	}
	
	/*
	 * Memory-efficient Levenshtein distance calculation:
	 * http://www.codeproject.com/Articles/13525/Fast-memory-efficient-Levenshtein-algorithm
	 * by Sten Hjelmqvist, 26 Mar 2012
	 */
	public static double levenshtein(String str1, String str2, boolean normalize) {
		// degenerate cases
		if(str1.equals(str2)) { return 0; }
		if(str1.length() == 0) { return str2.length(); }
		if(str2.length() == 0) { return str1.length(); }
		
		int maxLen = Math.max(str1.length(), str2.length());
		
		// working matrix
		int[] v0 = new int[maxLen];
		int[] v1 = new int[maxLen];
		
		// algorithm
		for(int i = 0; i <= maxLen; i++) {
			v0[i] = i;
		}
		
		for(int i = 0; i < str1.length(); i++) {
			v1[0] = i + 1;
			
			for(int j = 0; j < str2.length(); j++) {
				int cost;
				if(str1.charAt(i) == str2.charAt(j)) {
					cost = 0;
				} else {
					cost = 1;
				}
				v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost));
			}
			
			for(int j = 0; j < v0.length; j++) {
				v0[j] = v1[j];
			}
			
		}
		
		// return result
		if(normalize) {
			return v1[str2.length()] / maxLen;
		} else {
			return v1[str2.length()];
		}
		
	}
	
	/*
	 * Returns normalized Jaccard distance based on tri-grams, which is what Lucchese et al. are doing
	 * O(n^2) time complexity
	 */
	public static double jaccard(String str1, String str2) {
		
		return jaccard(str1, str2, 3);
	}
	
	public static double jaccard(String str1, String str2, int nGramSize) {
		
		// Normalized Jaccard distance formula given by:
		// 1 - ( intersection -> no. of ngrams in both strings ) / ( union -> no. ngrams in both strings)
		
		int intersection = 0;
		// union = str1NumOfNgrams + str2NumOfNgrams
		
		// Prep the ngram arrays
		int str1NumOfNgrams = str1.length() - nGramSize + 1;
		int str2NumOfNgrams = str2.length() - nGramSize + 1;
		String[] str1Ngrams = new String[str1NumOfNgrams];
		String[] str2Ngrams = new String[str2NumOfNgrams];
		
		// Generate the ngrams
		for(int i = 0 ; i < str1NumOfNgrams; i++) {
			str1Ngrams[i] = str1.substring(i, i + nGramSize - 1);
		}
		for(int i = 0; i < str2NumOfNgrams; i++) {
			str2Ngrams[i] = str2.substring(i, i + nGramSize - 1);
		}
		
		// Jaccard distance
		for(String str1Ngram : str1Ngrams) {
			for(String str2Ngram : str2Ngrams) {
				if(str1Ngram.equals(str2Ngram)) {
					intersection++;
				}
			}
		}
		
		return 1 - (intersection / (str1NumOfNgrams + str2NumOfNgrams));
		
	}
	
}
