package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;

public class Repeater extends AkkaActor {

    @Override
    public void handleData(Object value) throws Exception {
        broadcast(value);
    }
}
