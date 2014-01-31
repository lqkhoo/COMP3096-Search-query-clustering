/**
 * Class that calculates the query distance between two strings
 * 
 * @author Li Quan Khoo
 *
 */
public class QueryDistance {
	
	public QueryDistance() {
		
	}
	
	public static float Levenshtein(String str1, String str2) {
		return Levenshtein(str1, str2, true);
	}
	
	/*
	 * Memory-efficient Levenshtein distance calculation:
	 * http://www.codeproject.com/Articles/13525/Fast-memory-efficient-Levenshtein-algorithm
	 * by Sten Hjelmqvist, 26 Mar 2012
	 */
	public static float Levenshtein(String str1, String str2, boolean normalize) {
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
	
	public static float Jaccard(String str1, String str2) {
		// Return normalized Jaccard distance based on tri-grams, which is what Lucchese et al. are doing
		return Jaccard(str1, str2, 3);
	}
	
	public static float Jaccard(String str1, String str2, int nGramSize) {
		
		// Jaccard distance formula given by:
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
