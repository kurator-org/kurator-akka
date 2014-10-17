package org.kurator.akka;

import java.util.LinkedList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public abstract class BroadcastActor extends UntypedActor {

    final List<ActorRef> listeners = new LinkedList<ActorRef>();

    @Override
    public void onReceive(Object message) {

        if (message instanceof AddReceiver) {
            listeners.add(((AddReceiver) message).get());
        }

        if (message instanceof Initialize) {
            getSender().tell(new Response(), getSelf());
        }
    }

    protected void broadcast(Object message) {
        for (ActorRef listener : listeners) {
            listener.tell(message, this.getSelf());
        }
    }
}