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

import org.kurator.akka.data.WorkflowProduct;
import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.ExceptionMessage;
import org.kurator.akka.messages.Failure;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.ProductPublication;
import org.kurator.akka.messages.Start;
import org.kurator.akka.messages.Success;
import org.kurator.log.Logger;
import org.kurator.log.SilentLogger;
import org.kurator.log.Log;

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
    private final WorkflowRunner runner;
    private List<WorkflowProduct> products = new LinkedList<WorkflowProduct>();

    final Map<ActorRef, Set<ActorRef>> actorConnections = new HashMap<ActorRef, Set<ActorRef>>();

    public Workflow(ActorSystem actorSystem, String name, InputStream inStream, PrintStream outStream, PrintStream errStream, WorkflowRunner workflowRunner) {
        this.actorSystem = actorSystem;
        this.name = name;
        this.inStream = inStream;
        this.outStream = outStream;
        this.errStream = errStream;
        this.runner = workflowRunner;
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
        logger.trace("Now watching " + Log.ACTOR(runner.name(actor)));
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
        final ArrayList<String> messagedActors = new ArrayList<String>();
        Initialize initialize = new Initialize();
        for (ActorRef actor : actors) {
            logger.comm("Sending INITIALIZE message to " + Log.ACTOR(runner.name(actor)));
            responseFutures.add(ask(actor, initialize, Constants.TIMEOUT));
            messagedActors.add(runner.name(actor));
        }

        List<Failure> failures = new LinkedList<Failure>();
        
        // wait for success or failure response from each actor
        for (int i = 0; i < responseFutures.size(); ++i) {
            Future<Object> responseFuture = responseFutures.get(i);
            String actorName = messagedActors.get(i);
            responseFuture.ready(Constants.TIMEOUT_DURATION, null);
            logger.comm("Waiting for INITIALIZE response from " + Log.ACTOR(actorName));
            ControlMessage message = (ControlMessage)responseFuture.value().get().get();
            logger.comm("Received INITIALIZE response from " + Log.ACTOR(actorName));
            if (message instanceof Failure) {
                logger.error("Actor reports error during initialization: " + message);
                failures.add((Failure)message);
            }
        }

        ControlMessage result = (failures.size() == 0) ? 
                new Success() : new Failure("Error initializing workflow '" + name + "'" , failures);

        logger.debug("Done initializing actors");
        logger.comm("Sending INITIALIZE response to RUNNER");
        getSender().tell(result, getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {

        logger.trace("Received message of type " + message.getClass() + " from " + getSender());
        logger.value("Received message: ", message.toString());
        
        if (message instanceof Initialize) {
            logger.comm("Handling INITIALIZE message from RUNNER");
            initialize();
            logger.comm("Done handling INITIALIZE message");
            return;
        }

        if (message instanceof Start) {
            logger.comm("Handling START message from RUNNER");
            logger.debug("Starting actors");
            for (ActorRef actor : actors) {
                logger.trace("Sending START message to " + Log.ACTOR(runner.name(actor)));
                actor.tell(message, getSelf());
            }
            logger.debug("Run starting with " + actors.size() + " active actors");
            logger.comm("Done handling START message");
            return;
        }
        
        if (message instanceof ProductPublication) {
            logger.comm("Received PUBLISH_PRODUCT message");
            ProductPublication p = (ProductPublication) message;
            logger.value("Adding to list of workflow products:", p.product.label, p.product.value);
            products.add(p.product);
            logger.trace("Workflow has yielded " + products.size() + " products so far.");
            logger.comm("Done handling PUBLISH_PRODUCT message");
            return;
        }
        
        if (message instanceof ExceptionMessage) {
            ExceptionMessage em = (ExceptionMessage)message;
            errStream.println(sender() + " threw an uncaught exception:");
            Exception e = em.getException();
            e.printStackTrace(errStream);
            runner.setLastException(e);
            return;
        }
        
        if (message instanceof Terminated) {
            Terminated t = (Terminated) message;
            ActorRef actor = t.actor();
            logger.trace("Handling TERMINATED message from " + Log.ACTOR(runner.name(actor)));
            actors.remove(actor);
            logger.debug(Log.ACTOR(runner.name(actor)) + " has stopped");
            logger.debug("Number of active actors is now " + actors.size());
            if (actors.size() == 0) {
                logger.trace("Stopping because all actors have stopped");

                logger.debug("Stopping WORKFLOW");
                getContext().stop(getSelf());

                logger.debug("Shutting down ActorSystem");
                actorSystem.shutdown();
                
                logger.comm("Sending " + products.size() + " workflow PRODUCTS to RUNNER");
                runner.setWorkflowProducts(products);

            } else {
                logger.debug("Currently active actors include " + activeActors(3));
            }
            return;
        }
        
        if (inputActor != null) {
            logger.comm("Forwarding unhandled message to input actor " +  Log.ACTOR(runner.name(inputActor)));
            inputActor.tell(message, getSelf());
        }
    }

    private String activeActors(int max) {
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (ActorRef actor : actors) {
            if (++i > max) {
                buffer.append(" ...");
                break;
            }
            if (buffer.length() > 2) buffer.append(", ");
            buffer.append(Log.ACTOR(runner.name(actor)));
        }
        return buffer.toString();
    }    
}
