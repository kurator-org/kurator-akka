package org.kurator.akka;

public class Multiplier extends BroadcastActor {

    private Integer factor;

    public Multiplier(Integer factor) {
        super();
        this.factor = factor;
    }

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
