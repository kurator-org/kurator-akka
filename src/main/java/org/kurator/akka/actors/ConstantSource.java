package org.kurator.akka.actors;

import java.util.Collection;

import org.kurator.akka.messages.EndOfStream;

public class ConstantSource extends BroadcastActor {

    public Object value;
    public Collection<Object> values;
    public boolean sendEos = true;
    
    @Override
    public void handleStart() throws Exception {
        
        if (value != null) {
            broadcast(value);
        } else if (values != null) {
            for (Object value : values) {
                broadcast(value);                    
            }
        }
        
        if (sendEos) {
            broadcast(new EndOfStream());
        }
        
        getContext().stop(getSelf());
    }
}
