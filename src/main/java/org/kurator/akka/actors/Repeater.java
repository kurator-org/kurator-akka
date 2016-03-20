package org.kurator.akka.actors;

import org.kurator.akka.KuratorActor;

public class Repeater extends KuratorActor {

    @Override
    public void onData(Object value) throws Exception {
        broadcast(value);
    }
}
