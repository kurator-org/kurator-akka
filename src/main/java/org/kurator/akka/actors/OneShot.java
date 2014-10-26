package org.kurator.akka.actors;


public class OneShot extends BroadcastActor {

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof Integer) {
            broadcast(message);
            getContext().stop(getSelf());
        }
    }
}
