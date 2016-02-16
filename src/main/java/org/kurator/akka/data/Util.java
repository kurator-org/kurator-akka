package org.kurator.akka.data;

public class Util {
	
	/**
	 * Test to see if a string has non whitespace content.
	 * 
	 * @param s the string to test for content
	 * @return true if the string has zero length or contains only whitespace.
	 * @throws NullPointerException it s is null.
	 */
	public static boolean isBlank(String s) throws NullPointerException {
		return s.trim().isEmpty();
	}
	
	/**
	 * Test to see if a string has content.  
	 * 
	 * @param s the string to test for content.
	 * @return true if the string is not null and not blank, 
	 *    false if s is null or the string has zero length or 
	 *    if the string contains only whitespace.
	 */
	public static boolean hasContent(String s) {
		boolean result = true;
		try { 
			result = !isBlank(s);
		} catch (NullPointerException e){ 
			result = false;
		}
		return result;
	}
}
