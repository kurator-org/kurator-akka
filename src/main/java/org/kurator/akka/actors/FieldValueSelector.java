package org.kurator.akka.actors;

import java.util.HashMap;
import java.util.Map;

public class FieldValueSelector extends MapRecordSelector {

	public Map<String,String> requiredValues = new HashMap<String,String>();
	
    public boolean selects(Map<? extends String, ? extends String> record) {
    	for (Map.Entry<String, String> requirement : requiredValues.entrySet()) {
    	    String fieldName = requirement.getKey();
            if (!record.containsKey(fieldName)) return false;
    	    String requiredValue = requirement.getValue();
    	    String actualValue = record.get(fieldName);
    	    if (actualValue == null) return false;
    		if (!actualValue.equals(requiredValue)) return false;
    	}
    	return true;
    }
}
