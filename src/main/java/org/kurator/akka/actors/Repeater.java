package org.kurator.akka.actors;

public class Repeater extends Transformer {

    @Override
    public void handleDataMessage(Object message) throws Exception {
        broadcast(message);
    }
}
