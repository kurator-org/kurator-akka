package org.kurator.akka.actors;

import org.kurator.akka.messages.EndOfStream;

public class Multiplier extends BroadcastActor {

    public int factor = 1;

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof Integer) {
            Integer product = (Integer) message * this.factor;
            broadcast(product);
        } else if (message instanceof EndOfStream) {
            broadcast(message);
            getContext().stop(getSelf());
        }
    }
}
