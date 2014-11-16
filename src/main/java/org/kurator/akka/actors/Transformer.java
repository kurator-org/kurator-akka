package org.kurator.akka.actors;

import org.kurator.akka.messages.ControlMessage;

public abstract class Transformer extends AkkaActor {

    public void handleDataMessage(Object message) throws Exception {}
    
    @Override
    public void onReceive(Object message) throws Exception {

        super.onReceive(message);
        
        try {
    
            if (message instanceof ControlMessage) {
                handleControlMessage((ControlMessage)message);
            } else {
                handleDataMessage(message);
            }
            
        } catch (Exception e) {

            endStreamAndStop();
        }
    }
}