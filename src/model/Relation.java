package model;

/**
 * RDF relation representation
 * @author Li Quan Khoo
 *
 */
public class Relation {
	
	protected String name;	// name of the relation e.g. rdf:type
	
	public Relation(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}
	
}
