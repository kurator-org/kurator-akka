package org.kurator.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.util.Timeout;
import static akka.pattern.Patterns.ask;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class WorkflowDirector extends UntypedActor {

	ActorSystem actorSystem;
	Set<ActorRef> activeActors = new HashSet<ActorRef>();
	
	final List<String> actorNames = new LinkedList<String>();

	
	public WorkflowDirector(ActorSystem actorSystem) {
		this.actorSystem = actorSystem;
	}
	
	public void monitor(String actorName) {
		actorNames.add(actorName);
	}
		
	@Override
	public void onReceive(Object message) throws Exception {

		final Duration timeoutDuration = Duration.create(500, TimeUnit.SECONDS);
		final Timeout timeout = new Timeout(Duration.create(500, TimeUnit.SECONDS));
//		final ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>();
		
    	if (message instanceof Initialize) {
	    	for (String name : actorNames) {
	    		ActorRef actor = getContext().system().actorFor("/user/" + name);
	    		activeActors.add(actor);
	    		getContext().watch(actor);
	    		Future<Object> future = ask(actor, message, timeout);
	    		future.ready(timeoutDuration, null);
	    	}
        	getSender().tell(message, getSelf());
	    	return;
    	}
		
		if (message instanceof Terminated) {
			Terminated t = (Terminated)message;
			ActorRef terminatedActor = t.actor();
			activeActors.remove(terminatedActor);
			if (activeActors.size() == 0) {
                getContext().stop(getSelf());
				actorSystem.shutdown();
			}
		}
	}
}
