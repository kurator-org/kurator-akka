package org.kurator.akka.actors;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.ExceptionMessage;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Response;
import org.kurator.akka.messages.StartMessage;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/** 
 * Base class for all actors that can run within the kurator-akka framework.  
 * 
 * <p> This class standardizes the actor lifecycle and maintains the list of listeners for each actor.
 * It also provides the option of automatically stopping an actor when an <code>EndOfStream</code> 
 * message is received, and for automatically propagating the <code>EndOfStream</code> message to listeners.
 * The class thus supports clean shutdown of Akka workflows with each actor terminating itself when no 
 * further messages from upstream can be expected. </p>
 */

public abstract class AkkaActor extends UntypedActor {

    /** Determines if actor automatically terminates when it receives an EndOfStream message.
     *  Defaults to true. */
    public boolean endOnEos = true;
    
    /** Stream used by actor instead of writing to System.out directly. Defaults to System.out. 
     * Non-default value assigned via setter. */
    protected PrintStream outStream = System.out;

    /** Stream used by actor instead of writing to System.err directly. Defaults to System.err.
     * Non-default value assigned via setter */
    protected PrintStream errStream = System.err;

    // private fields
    private List<ActorBuilder> listenerBuilders = new LinkedList<ActorBuilder>();
    private Set<ActorRef> listeners = new HashSet<ActorRef>(); 
    private WorkflowBuilder runner;
    

    public AkkaActor listeners(List<ActorBuilder> listenerBuilders) {
        if (listenerBuilders != null) {
            this.listenerBuilders = listenerBuilders;
        }
        return this;
    }
    
    public AkkaActor runner(WorkflowBuilder runner) {
        this.runner = runner;
        return this;
    }
    
    public AkkaActor errorStream(PrintStream errStream) {
        this.errStream = errStream;
        return this;
    }
    
    public AkkaActor outputStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }    

    @Override
    public void onReceive(Object message) throws Exception {

        try {
            
            if (message instanceof ControlMessage) {
                
                if (message instanceof Initialize) {
                
                    for (ActorBuilder listenerConfig : listenerBuilders) {
                        ActorRef listener = runner.getActorForConfig(listenerConfig);
                        listeners.add(listener);
                    }
                                
                    getSender().tell(new Response(), getSelf());
                    
                    handleInitialize();
                    
                } else if (message instanceof StartMessage) {
                    
                    handleStart();
                
                } else if (message instanceof EndOfStream) {
                    handleEndOfStream((EndOfStream)message);
                }            
            } else {
                handleDataMessage(message);
            }
            
        } catch (Exception e) {
            reportException(e);
            endStreamAndStop();
        }
    }
    
    /** 
     * Empty default handler for <i>Initialize</i> event.  Called when the actor receives a
     * <code>Initialize</code> message.
     * 
     * This method can be overridden by child classes to perform any tasks that must occur before 
     * the workflow begins to execute.  A workflow begins executing (and a <code>Start</code> message
     * is sent to each actor) only after <i>all</i> actors in the workflow receive the 
     * <code>Initialize</code> message and return from this handler.
     * 
     * @throws Exception
     */
    protected void handleInitialize() throws Exception {}
        
    /** 
     * Empty default handler for <i>Start</i> event.  Called when the actor receives a
     * <code>Start</code> message.
     * 
     * <p> Can be overridden by children classes to perform any tasks that must occur once at the beginning 
     * of a workflow run but after all actors have been initialized. Actors that handle the <code>Start</code> 
     * message can bootstrap the execution of a workflow by peforming computations and sending one or more
     * messages before receiving messages from other actors in the workflow.</p>
     * 
     * <p> Note that if an actor is a listener of another actor in the workflow then it is <i>not</i> guaranteed 
     * to receive the <code>Start</code> message before receiving messages from other actors. Thus this method
     * is most useful when an actor is not listener or when the actor occurs in a workflow cycle such that the 
     * messages it receives are produced in response to the messages that it sends.</p>
     * 
     * <p> Note also that if an actor is not a listener of any other actor then it may delay returning from
     * this method until the actor has performed all of its activity for the workflow run. Thus, an actor serving
     * as a data source for a workflow may in some cases perform all of its work in handleStart(). </p>
     * 
     * @throws Exception
     */
    protected void handleStart() throws Exception {}
    
    protected void handleEndOfStream(EndOfStream message) throws Exception {
        if (endOnEos) {
            handleEnd();
            endStreamAndStop((EndOfStream)message);
        }      
    }
    
    protected void handleEnd() throws Exception {}
    
    protected void handleDataMessage(Object message) throws Exception {}
    
    protected void broadcast(Object message) {
        for (ActorRef listener : listeners) {
            listener.tell(message, this.getSelf());
        }
    }
    
    protected void endStreamAndStop(EndOfStream message) { 
        broadcast(message);
        stop();
    }
    
    protected void endStreamAndStop() { 
        endStreamAndStop(new EndOfStream());
    }
    
    protected void stop() {
        getContext().stop(getSelf());
    }
    
    protected void reportException(Exception e) {
        ActorRef workflowRef = runner.getWorkflowRef();
        ExceptionMessage em = new ExceptionMessage(e);
        workflowRef.tell(em, this.getSelf());
    }
}
