package org.kurator.akka;

public class Repeater extends BroadcastActor {

    boolean eosSent = false;

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
