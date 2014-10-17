package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.util.Timeout;

public class Workflow extends UntypedActor {

    ActorSystem actorSystem;
    Set<ActorRef> activeActors = new HashSet<ActorRef>();

    final List<String> actorNames = new LinkedList<String>();
    final Map<String, Set<String>> actorConnections = new HashMap<String, Set<String>>();

    public Workflow(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public void actor(String actorName) {
        actorNames.add(actorName);
    }

    public void connection(String sender, String receiver) {
        Set<String> receivers = actorConnections.get(sender);
        if (receivers == null) {
            receivers = new HashSet<String>();
            actorConnections.put(sender, receivers);
        }
        receivers.add(receiver);
    }

    private void createWorkflowGraph() throws TimeoutException,
            InterruptedException {

        final FiniteDuration timeoutDuration = Duration.create(
                Properties.TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final Timeout timeout = new Timeout(timeoutDuration);
        final ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>();

        Initialize initialize = new Initialize();

        for (String name : actorNames) {
            ActorRef actor = getContext().system().actorFor("/user/" + name);
            activeActors.add(actor);
            getContext().watch(actor);

            Set<String> receivers = actorConnections.get(name);
            if (receivers != null) {
                for (String receiver : receivers) {
                    actor.tell(new AddReceiver(receiver), getSelf());
                }
            }

            futures.add(ask(actor, initialize, timeout));
        }

        for (Future<Object> future : futures) {
            future.ready(timeoutDuration, null);
        }

        getSender().tell(initialize, getSelf());

    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Initialize) {
            createWorkflowGraph();
            return;
        }

        if (message instanceof Terminated) {
            Terminated t = (Terminated) message;
            ActorRef terminatedActor = t.actor();
            activeActors.remove(terminatedActor);
            if (activeActors.size() == 0) {
                getContext().stop(getSelf());
                actorSystem.shutdown();
            }
        }
    }
}
