package org.kurator.akka.actors;

import java.util.Map;

import org.kurator.akka.data.Util;

public class RequiredFieldsRecordFilter extends MapRecordFilter {

	public String[] requiredFields;
	public boolean disallowEmptyFields = true;
	
	@Override
	public void handleInitialize() {
	    if (requiredFields == null) {
	       requiredFields = new String[] {};
	    }
	}

    public boolean accepts(Map<? extends String, ? extends String> record) {
    	for (String key : requiredFields) {
    		if (!record.containsKey(key)) return false;
    		if (disallowEmptyFields && !Util.hasContent(record.get(key))) return false;
    	}
    	return true;
    }
}
