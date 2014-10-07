package org.kurator.akka;

import akka.actor.UntypedActor;
import akka.routing.Broadcast;

/* 
 * NOTE: This code was derived from akka.TextDisplay in the FilteredPush repository at
 * svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-Akka as of 07Oct2014. 
 */

class TextDisplay extends UntypedActor {
    int invoc = 0;
    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
        if (message instanceof Token) {
            System.out.println(((Token) message).getData());
        } else if (message instanceof Broadcast) {
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        invoc++;
    }

    @Override
    public void postStop() {
        getContext().system().shutdown();
        super.postStop();
    }
}
