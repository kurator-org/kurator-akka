package org.kurator.akka;

import akka.actor.ActorRef;

public class AddReceiver {

    private final ActorRef receiver;

    public AddReceiver(ActorRef receiver) {
        this.receiver = receiver;
    }

    public ActorRef get() {
        return receiver;
    }
}
