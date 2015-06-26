package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;

public class Repeater extends AkkaActor {

    @Override
    public void onData(Object value) throws Exception {
        broadcast(value);
    }
}
