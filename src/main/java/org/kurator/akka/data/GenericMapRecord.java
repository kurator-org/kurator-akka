package org.kurator.akka.data;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class GenericMapRecord extends LinkedHashMap<String,String> {

    public GenericMapRecord() {
        super();
    }
	
	public GenericMapRecord(Map<? extends String, ? extends String> map) {
        super(map);
    }
    
	public GenericMapRecord(String[] values) {
		super();
		int fieldCount = values.length / 2;
		for (int i = 0; i < fieldCount; ++ i) {
			String key = values[i*2];
			String value = values[i*2+1];
			this.put(key, value);
		}
	}
}
