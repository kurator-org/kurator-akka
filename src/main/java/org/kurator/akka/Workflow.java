package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class Workflow extends UntypedActor {

    ActorSystem actorSystem;
    Set<ActorRef> actors = new HashSet<ActorRef>();

    final Map<ActorRef, Set<ActorRef>> actorConnections = new HashMap<ActorRef, Set<ActorRef>>();

    public Workflow(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public void actor(ActorRef actor) {
        actors.add(actor);
        getContext().watch(actor);
    }

    public void connection(ActorRef sender, ActorRef receiver) {
        Set<ActorRef> receivers = actorConnections.get(sender);
        if (receivers == null) {
            receivers = new HashSet<ActorRef>();
            actorConnections.put(sender, receivers);
        }
        receivers.add(receiver);
    }

    private void elaborate() throws TimeoutException, InterruptedException {

        // inform each actor in workflow of its receivers
        for (Map.Entry<ActorRef, Set<ActorRef>> e : actorConnections.entrySet()) {
            ActorRef sender = e.getKey();
            for (ActorRef receiver : e.getValue()) {
                sender.tell(new AddReceiver(receiver), getSelf());
            }
        }

        // send an initialize message to each actor
        final ArrayList<Future<Object>> responseFutures = new ArrayList<Future<Object>>();
        Initialize initialize = new Initialize();
        for (ActorRef a : actors) {
            responseFutures.add(ask(a, initialize, Constants.TIMEOUT));
        }

        // wait for response for initialization for each actor
        for (Future<Object> responseFuture : responseFutures) {
            responseFuture.ready(Constants.TIMEOUT_DURATION, null);
        }

        getSender().tell(new Response(), getSelf());

    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Initialize) {
            elaborate();
            return;
        }

        if (message instanceof Terminated) {
            Terminated t = (Terminated) message;
            ActorRef terminatedActor = t.actor();
            actors.remove(terminatedActor);
            if (actors.size() == 0) {
                getContext().stop(getSelf());
                actorSystem.shutdown();
            }
        }
    }
}
