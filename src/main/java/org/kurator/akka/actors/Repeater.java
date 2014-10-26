package org.kurator.akka.actors;

import org.kurator.akka.messages.EndOfStream;

public class Repeater extends BroadcastActor {

    private boolean eosSent = false;

    @Override
    public void onReceive(Object message) {

        super.onReceive(message);

        if (message instanceof EndOfStream) {
            broadcast(message);
            getContext().stop(getSelf());
            return;
        }

        if (message instanceof Integer && !eosSent)
            broadcast(message);
    }
}
