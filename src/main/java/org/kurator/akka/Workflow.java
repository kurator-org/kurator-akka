package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.ExceptionMessage;
import org.kurator.akka.messages.Failure;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Start;
import org.kurator.akka.messages.Success;
import org.kurator.log.Logger;
import org.kurator.log.SilentLogger;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class Workflow extends UntypedActor {

    private final ActorSystem actorSystem;
    private final Set<ActorRef> actors = new HashSet<ActorRef>();
    private final String name;
    private ActorRef inputActor;
    private Logger logger = new SilentLogger();

    @SuppressWarnings("unused")
    private final InputStream inStream;
    @SuppressWarnings("unused")
    private final PrintStream outStream;
    private final PrintStream errStream;
    private final WorkflowRunner workflowRunner;
    
    final Map<ActorRef, Set<ActorRef>> actorConnections = new HashMap<ActorRef, Set<ActorRef>>();

    public Workflow(ActorSystem actorSystem, String name, InputStream inStream, PrintStream outStream, PrintStream errStream, WorkflowRunner workflowRunner) {
        this.actorSystem = actorSystem;
        this.name = name;
        this.inStream = inStream;
        this.outStream = outStream;
        this.errStream = errStream;
        this.workflowRunner = workflowRunner;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
        this.logger.setSource("WORKFLOW");
    }
    
    public void setInput(ActorRef inputActor) {
        this.inputActor = inputActor;
    }

    private void actor(ActorRef actor) {
        actors.add(actor);
        logger.trace("Now watching ACTOR");
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

    private void initialize() throws Exception {

        logger.debug("Initializing actors");
        
        // send an initialize message to each actor
        final ArrayList<Future<Object>> responseFutures = new ArrayList<Future<Object>>();
        Initialize initialize = new Initialize();
        for (ActorRef actor : actors) {
            logger.trace("Sending INITIALIZE message to ACTOR");
            responseFutures.add(ask(actor, initialize, Constants.TIMEOUT));
        }

        List<Failure> failures = new LinkedList<Failure>();
        
        // wait for success or failure response from each actor
        for (Future<Object> responseFuture : responseFutures) {
            responseFuture.ready(Constants.TIMEOUT_DURATION, null);
            logger.trace("Waiting for INITIALIZE response from ACTOR");
            ControlMessage message = (ControlMessage)responseFuture.value().get().get();
            logger.trace("Received INITIALIZE response from ACTOR");
            if (message instanceof Failure) {
                logger.error("Actor reports error during initialization: " + message);
                failures.add((Failure)message);
            }
        }

        ControlMessage result = (failures.size() == 0) ? 
                new Success() : new Failure("Error initializing workflow '" + name + "'" , failures);

        logger.trace("Sending INITIALIZE response to RUNNER");
        getSender().tell(result, getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Initialize) {
            logger.trace("Handling INITIALIZE message from RUNNER");
            initialize();
            logger.trace("Done handling INITIALIZE message");
            return;
        }

        if (message instanceof Start) {
            logger.trace("Handling START message from RUNNER");
            logger.debug("Starting actors");
            for (ActorRef a : actors) {
                logger.trace("Sending START message to ACTOR ");
                a.tell(message, getSelf());
            }
            logger.debug("Run starting with " + actors.size() + " active actors");
            logger.trace("Done handling START message");
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
            logger.trace("Handling TERMINATED message from ACTOR");
            actors.remove(terminatedActor);
            logger.debug("ACTOR has stopped");
            logger.debug("Number of active actors is now " + actors.size());
            if (actors.size() == 0) {
                logger.trace("Stopping because all actors have stopped");
                getContext().stop(getSelf());
                logger.debug("Shutting down ActorSystem");
                actorSystem.shutdown();
            }
            return;
        }
        
        if (inputActor != null) {
            logger.debug("Forwarding unhandled message to actor");
            inputActor.tell(message, getSelf());
        }
    }
}
