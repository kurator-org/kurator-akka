package org.kurator.akka.actors;

public class Multiplier extends AkkaActor {

    public int factor = 1;

    @Override
    public void handleDataMessage(Object message) throws Exception {
        if (message instanceof Integer) {
            Integer product = (Integer) message * this.factor;
            broadcast(product);
        }
    }
}
