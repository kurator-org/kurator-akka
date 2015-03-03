package org.kurator.akka.data;

public class Util {
	
	public static boolean isBlank(String s) {
		return s.trim().isEmpty();
	}
	
	public static boolean hasContent(String s) {
		return s != null && ! isBlank(s);
	}
}
