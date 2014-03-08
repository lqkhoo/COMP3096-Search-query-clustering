package model;

import java.util.HashSet;

import gnu.trove.set.hash.TIntHashSet;

/**
 * Model of mapping strength between session and class. Used by SessionToClassMapping
 * @author Li Quan Khoo
 */
@Deprecated
public class SessionFullMapping {
	
	public final String className;
	public final HashSet<Integer> map;
	
	public SessionFullMapping(String className, HashSet<Integer> hashSet) {
		this.className = className;
		this.map = hashSet;
	}
}
