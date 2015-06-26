package org.kurator.akka.actors;

import java.util.Map;

import org.kurator.akka.AkkaActor;

public abstract class MapRecordSelector extends AkkaActor {

    protected abstract boolean selects(Map<? extends String, ? extends String> record);
    
	@Override
    @SuppressWarnings("unchecked")
    public void onData(Object value) throws Exception {
        if (value instanceof Map<?,?> && selects((Map<String,String>)value)) {
        	broadcast(value);
        }
    }
}
