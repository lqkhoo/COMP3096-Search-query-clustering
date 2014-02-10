package model;

/**
 * Immutable. Wrapper.
 * The type of Strings output by YagoReader and subclasses. Typically contains additional tokens 
 * from the original file like "<" and ">". It is the responsibility of the receiving
 * class to turn these strings into meaningful object representations.
 * @author Li Quan Khoo
 *
 */
public class RawString {
	public final String STRING;
	public RawString(String string) {
		this.STRING = string;
	}
}
