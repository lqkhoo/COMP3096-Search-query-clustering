package model;

/**
 * Abstract.
 * 
 * The top level class all members involved in both sides of an RDF relation belongs to, e.g.
 * A and B in this relation:
 * 
 * A rdf:type B, no matter what A and B are.
 * @author Li Quan Khoo
 *
 */
public abstract class ATopLevelEntity {
	
	protected String name;
	protected RawString raw;
	
}
