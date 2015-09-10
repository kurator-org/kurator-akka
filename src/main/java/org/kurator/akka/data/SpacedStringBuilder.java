package org.kurator.akka.data;

public class SpacedStringBuilder {
	
	private StringBuilder sb = new StringBuilder();
	private String spacer = " ";

	public SpacedStringBuilder() {
    }

	public SpacedStringBuilder(String spacer) {
	    this.spacer = spacer;
	}
	
	public SpacedStringBuilder append(String s) { 
		if (Util.hasContent(s)) {
			if (sb.length() > 0) sb.append(spacer);
			sb.append(s);
		}
		return this;
	}

	public SpacedStringBuilder appendSinglyQuoted(String s) { 
		if (Util.hasContent(s)) {
			if (sb.length() > 0) sb.append(spacer);
			sb.append("'")
			  .append(s)
			  .append("'");
		}
		return this;
	}

	
	public String toString() { return sb.toString(); }


}