package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;

public class Multiplier extends AkkaActor {

    public int factor = 1;

    @Override
    public void handleData(Object value) throws Exception {
        if (value instanceof Integer) {
            int product = (int) value * this.factor;
            broadcast(product);
        }
    }
}
