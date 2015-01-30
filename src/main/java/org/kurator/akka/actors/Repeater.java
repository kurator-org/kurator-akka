package org.kurator.akka.actors;

public class Repeater extends AkkaActor {

    @Override
    public void handleData(Object value) throws Exception {
        broadcast(value);
    }
}
