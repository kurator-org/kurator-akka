package org.kurator.akka.actors;

import java.util.Map;

import org.kurator.akka.KuratorActor;

public abstract class MapRecordSelector extends KuratorActor {

    protected abstract boolean selects(Map<? extends String, ? extends String> record);
    
	@Override
    @SuppressWarnings("unchecked")
    public void onData(Object value) throws Exception {
        if (value instanceof Map<?,?> && selects((Map<String,String>)value)) {
        	broadcast(value);
        }
    }
}
