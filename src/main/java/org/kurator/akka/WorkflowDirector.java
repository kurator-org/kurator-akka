package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.util.Timeout;

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

        final FiniteDuration timeoutDuration = Duration.create(5,
                TimeUnit.SECONDS);
        final Timeout timeout = new Timeout(timeoutDuration);
        final ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>();

        if (message instanceof Initialize) {

            for (String name : actorNames) {
                ActorRef actor = getContext().system()
                        .actorFor("/user/" + name);
                activeActors.add(actor);
                getContext().watch(actor);
                futures.add(ask(actor, message, timeout));
            }

            for (Future<Object> future : futures) {
                future.ready(timeoutDuration, null);
            }

            getSender().tell(message, getSelf());

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
