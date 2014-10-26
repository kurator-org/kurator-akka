package org.kurator.akka.actors;

import org.kurator.akka.messages.EndOfStream;

public class Filter extends BroadcastActor {

    public int max = 1;
    public boolean sendEosOnExceed = true;

    private boolean eosSent = false;

    @Override
    public void onReceive(Object message) {

        super.onReceive(message);

        if (message instanceof EndOfStream) {
            broadcast(message);
            getContext().stop(getSelf());
            return;
        }

        if (eosSent)
            return;

        if (message instanceof Integer) {
            Integer value = (Integer) message;
            if (value <= max) {
                broadcast(value);
            } else if (sendEosOnExceed) {
                eosSent = true;
                broadcast(new EndOfStream());
            }
        }
    }
}
