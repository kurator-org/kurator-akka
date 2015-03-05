package org.kurator.akka.actors;

import java.util.Map;

import org.kurator.akka.AkkaActor;

public abstract class MapRecordSelector extends AkkaActor {

    public abstract boolean accepts(Map<? extends String, ? extends String> record);
    
	@Override
    @SuppressWarnings("unchecked")
    public void handleData(Object value) throws Exception {
        if (value instanceof Map<?,?> && accepts((Map<String,String>)value)) {
        	broadcast(value);
        }
    }
}
