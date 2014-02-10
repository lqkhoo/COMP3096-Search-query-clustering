package model;

/**
 * Immutable.
 * A container class representing an RDF triple relation
 * @author Li Quan Khoo
 *
 */
public class Triple {
	
	public final ATopLevelEntity first;
	public final Relation relation;
	public final ATopLevelEntity second;
	
	public Triple(ATopLevelEntity first, Relation relation, ATopLevelEntity second) {
		this.first = first;
		this.relation = relation;
		this.second = second;
	}
}
