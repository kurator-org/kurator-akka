package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.messages.ExceptionMessage;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Response;
import org.kurator.akka.messages.Start;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class Workflow extends UntypedActor {

    ActorSystem actorSystem;
    Set<ActorRef> actors = new HashSet<ActorRef>();
    ActorRef inputActor;

    @SuppressWarnings("unused")
    private final InputStream inStream;
    private final PrintStream outStream;
    private final PrintStream errStream;
    private final WorkflowRunner workflowRunner;
    
    final Map<ActorRef, Set<ActorRef>> actorConnections = new HashMap<ActorRef, Set<ActorRef>>();

    public Workflow(ActorSystem actorSystem, InputStream inStream, PrintStream outStream, PrintStream errStream, WorkflowRunner workflowRunner) {
        this.actorSystem = actorSystem;
        this.inStream = inStream;
        this.outStream = outStream;
        this.errStream = errStream;
        this.workflowRunner = workflowRunner;
    }
    
    public void setInput(ActorRef inputActor) {
        this.inputActor = inputActor;
    }

    private void actor(ActorRef actor) {
        actors.add(actor);
        getContext().watch(actor);
    }

    public void setActors(Set<ActorRef> actors) {
        for (ActorRef actor : actors) {
            actor(actor);
        }
    }

    public void connection(ActorRef sender, ActorRef receiver) {
        Set<ActorRef> receivers = actorConnections.get(sender);
        if (receivers == null) {
            receivers = new HashSet<ActorRef>();
            actorConnections.put(sender, receivers);
        }
        receivers.add(receiver);
    }

    private void initialize() throws TimeoutException, InterruptedException {

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
            initialize();
            return;
        }

        if (message instanceof Start) {
            for (ActorRef a : actors) {
                a.tell(message, getSelf());
            }
            return;
        }
        
        if (message instanceof ExceptionMessage) {
            ExceptionMessage em = (ExceptionMessage)message;
            errStream.println(sender() + " threw an uncaught exception:");
            Exception e = em.getException();
            e.printStackTrace(errStream);
            workflowRunner.setLastException(e);
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
            return;
        }
        
        if (inputActor != null) {
            inputActor.tell(message, getSelf());
        }
    }
}
