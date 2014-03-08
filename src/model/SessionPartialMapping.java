package model;

import java.util.HashMap;

import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Model of mapping strength between session and class. Used by SessionToClassMapping
 * @author Li Quan Khoo
 */
@Deprecated
public class SessionPartialMapping {
	
	public final String className;
	public final HashMap<Integer, Integer> map;
	
	public SessionPartialMapping(String className, HashMap<Integer, Integer> map) {
		this.className = className;
		this.map = map;
	}
}
