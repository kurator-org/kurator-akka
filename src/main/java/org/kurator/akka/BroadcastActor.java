package org.kurator.akka;

import java.util.LinkedList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public abstract class BroadcastActor extends UntypedActor {

    final List<String> listenerNames = new LinkedList<String>();
    final List<ActorRef> listeners = new LinkedList<ActorRef>();

    public void addListener(String listenerName) {
        listenerNames.add(listenerName);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Initialize) {
            for (String name : listenerNames) {
                ActorRef listener = getContext().system().actorFor(
                        "/user/" + name);
                listeners.add(listener);
            }
            getSender().tell(message, getSelf());
        }
    }

    protected void broadcast(Object message) {
        for (ActorRef listener : listeners) {
            listener.tell(message, this.getSelf());
        }
    }
}