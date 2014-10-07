package org.kurator.akka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;

/* 
 * NOTE: This code was derived from akka.ConstActor in the FilteredPush repository at
 * svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-Akka as of 07Oct2014. 
 */

public class ConstActor extends UntypedActor {
    private final ActorRef listener;
    int invoc;
    long constant;

    public ConstActor(int i, ActorRef listener) {
        this.listener = listener;
        this.constant = i;
        invoc = 0;
    }

    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof Trigger) {
            listener.tell(new Token<Long>(constant),getSelf());
            getContext().stop(getSelf());
        } else if (message instanceof Broadcast) {
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        invoc++;
    }

    @Override
    public void postStop() {
        //getContext().system().shutdown();
        super.postStop();
    }

}
