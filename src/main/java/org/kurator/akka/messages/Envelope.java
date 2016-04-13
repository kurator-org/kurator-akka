package org.kurator.akka.messages;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Envelope extends LinkedHashMap<String,Object> {

    public Envelope() {
        super();
    }
	
	public Envelope(Map<? extends String, ? extends String> map) {
        super(map);
    }
    	
    public Envelope(Object... values) {
		super();
		int fieldCount = values.length / 2;
		for (int i = 0; i < fieldCount; ++ i) {
			String key = (String)values[i*2];
			Object value = values[i*2+1];
			this.put(key, value);
		}
	}
}
