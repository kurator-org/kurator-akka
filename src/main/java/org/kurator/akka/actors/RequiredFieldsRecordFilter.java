package org.kurator.akka.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kurator.akka.data.Util;

public class RequiredFieldsRecordFilter extends MapRecordFilter {

	public List<String> requiredFields = new ArrayList<String>();
	public boolean disallowEmptyFields = true;
	
    public boolean accepts(Map<? extends String, ? extends String> record) {
    	for (String key : requiredFields) {
    		if (!record.containsKey(key)) return false;
    		if (disallowEmptyFields && !Util.hasContent(record.get(key))) return false;
    	}
    	return true;
    }
}
