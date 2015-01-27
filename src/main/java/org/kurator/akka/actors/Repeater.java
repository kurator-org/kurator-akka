package org.kurator.akka.actors;

public class Repeater extends AkkaActor {

    @Override
    public void handleDataMessage(Object message) throws Exception {
        broadcast(message);
    }
}
