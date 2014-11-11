package org.kurator.akka.actors;

import org.kurator.akka.messages.EmptyMessage;
import org.kurator.akka.messages.EndOfStream;

public class OneShot extends BroadcastActor {

    public Object value = new EmptyMessage();
    public boolean sendEos = false;
    
    @Override
    public void handleStart() throws Exception {
        
        broadcast(value);
        
        if (sendEos) {
            broadcast(new EndOfStream());
        }
        
        getContext().stop(getSelf());
    }
}
