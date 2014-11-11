package org.kurator.akka.actors;

public class Repeater extends BroadcastActor {

    @Override
    public void handleDataMessage(Object message) throws Exception {
        broadcast(message);
    }
}
