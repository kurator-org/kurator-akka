package org.kurator.akka;

public class Filter extends BroadcastActor {

    int max;
    boolean eosSent = false;

    public Filter(Integer max) {
        super();
        this.max = max;
    }

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
            } else {
                eosSent = true;
                broadcast(new EndOfStream());
            }
        }
    }
}
