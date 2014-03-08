package model;

public class SearchStringToClassMapping {
	
	public final String searchString;
	public final Integer[] classNIds;
	
	public SearchStringToClassMapping(String searchString, Integer[] integers) {
		this.searchString = searchString;
		this.classNIds = integers;
	}
	
}
