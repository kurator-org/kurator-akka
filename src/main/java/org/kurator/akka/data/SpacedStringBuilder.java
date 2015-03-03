package org.kurator.akka.data;

public class SpacedStringBuilder {
	
	private StringBuilder sb = new StringBuilder();
		
	public SpacedStringBuilder append(String s) { 
		if (Util.hasContent(s)) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(s);
		}
		return this;
	}

	public SpacedStringBuilder appendSinglyQuoted(String s) { 
		if (Util.hasContent(s)) {
			if (sb.length() > 0) sb.append(" ");
			sb.append("'")
			  .append(s)
			  .append("'");
		}
		return this;
	}

	
	public String toString() { return sb.toString(); }


}