package org.kurator.akka;

public class Oneshot extends BroadcastActor {
	
    @Override
    public void onReceive(Object message) {
    	super.onReceive(message);
    	if (message instanceof Integer) {
	    	broadcast(message);
	        getContext().stop(getSelf());
    	}
    }
}
