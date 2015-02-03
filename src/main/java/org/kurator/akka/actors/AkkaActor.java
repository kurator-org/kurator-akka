package org.kurator.akka.actors;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.ExceptionMessage;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Response;
import org.kurator.akka.messages.Start;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/** 
 * Base class for all actors that can run within the Kurator-Akka workflow framework.
 * 
 * <p> This class standardizes the actor lifecycle and maintains the list of listeners for each actor.
 * It also provides the option of automatically stopping an actor when an 
 * {@link org.kurator.akka.messages.EndOfStream EndOfStream} 
 * message is received, and for automatically propagating the 
 * {@link org.kurator.akka.messages.EndOfStream EndOfStream} message to listeners.
 * The class thus supports clean shutdown of Akka workflows with each actor terminating itself when no 
 * further messages from upstream can be expected. </p>
 */
public abstract class AkkaActor extends UntypedActor {
    
    /** Shorthand for platform-specific end-of-line character sequence. */
    public static final String EOL = System.getProperty("line.separator");

    /** Determines if this actor automatically terminates when it receives an 
     * {@link org.kurator.akka.messages.EndOfStream EndOfStream} message.
     * Defaults to <i>true</i>. */
    public boolean endOnEos = true;

    /** Determines if this actor automatically sends an 
     * {@link org.kurator.akka.messages.EndOfStream EndOfStream} message to
     * all of its listeners just before it stops executing.
     * Defaults to <i>true</i>. */
    public boolean sendEosOnEnd = true;
    
    /** Stream used by actor instead of writing to <code>System.out</code> directly. 
     * Defaults to <code>System.out</code>. 
     * <p>Non-default value assigned can be assigned via the {@link #outputStream outputStream()} method.<p>
     */
    protected PrintStream outStream = System.out;

    /** Stream used by actor instead of writing to <code>System.err</code> directly. 
     * Defaults to <code>System.err</code>. 
     * <p>Non-default value can be assigned via the {@link #errorStream errorStream()} method.</p>
     */
    protected PrintStream errStream = System.err;

    // private fields
    private List<ActorConfig> listenerConfigs = new LinkedList<ActorConfig>();
    private Set<ActorRef> listeners = new HashSet<ActorRef>();
    private WorkflowRunner runner;

    
    

    
    /** 
     * Specifies the list of listeners for this actor in the current workflow.
     * 
     * <p>The input parameter is given in terms of {@link org.kurator.akka.ActorConfig ActorConfig} 
     * instances (rather than {@link akka.actor.ActorRef ActorRef} instances)
     * because the actors may not have been constructed yet.  The ActorRef corresponding
     * to each ActorConfig is looked up and the list of listeners in terms of ActorRef 
     * instances composed by {@link #onReceive(Object) onReceive()} when the 
     * {@link org.kurator.akka.messages.Initialize Initialize} message is received.</p>
     * 
     * @param listenerConfigs The list of actor configurations corresponding to this actor's listeners.
     * @return this AkkaActor
     */
    public AkkaActor listeners(List<ActorConfig> listenerConfigs) {
        if (listenerConfigs != null) {
            this.listenerConfigs = listenerConfigs;
        }
        return this;
    }
    
    /** 
     * Specifies the {@link org.kurator.akka.WorkflowRunner WorkflowRunner} for the current workflow.
     * 
     * <p>The workflow runner is used for accessing the mapping of listener {@link org.kurator.akka.ActorConfig ActorConfig} 
     * instances to {@link akka.actor.ActorRef ActorRef} instances, and for reporting exceptions to the runner.</p>
     * 
     * @param runner The {@link org.kurator.akka.WorkflowRunner WorkflowRunner} that built and is currently 
     *               executing the workflow containing this actor.
     * @return this AkkaActor
     */
    public AkkaActor runner(WorkflowRunner runner) {
        this.runner = runner;
        return this;
    }
    
    
    /** 
     * Specifies the output stream to be used by an actor that needs
     * to write to <code>stderr</code>. The value is stored in {@link #errStream}.
     * 
     * <p>Child classes should write error messages to {@link #errStream} instead of 
     * writing to <code>System.err</code> directly.</p>
     * 
     * @param errStream The PrintStream to use for writing to <code>stderr</code>.
     * @return this AkkaActor
     */   
    public AkkaActor errorStream(PrintStream errStream) {
        this.errStream = errStream;
        return this;
    }
    
    
    /** 
     * Specifies the output stream to be used by an actor that needs
     * to write to <code>stdout</code>. The value is stored in {@link #outStream}.
     * 
     * <p>Child classes should send output to {@link #outStream} instead of 
     * writing to <code>System.out</code> directly.</p>
     * 
     * @param outStream The PrintStream to use for writing to <code>stdout</code>.
     * @return this AkkaActor
     */
    public AkkaActor outputStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }    

    
    /** 
     * Initial handler for all messages received by this actor via the Akka framework.  
     * 
     * <p> This method may not be overridden by child classes.  Non-default responses to
     * messages can provided by overriding one or more of {@link #handleInitialize()}, 
     * {@link #handleStart()}, {@link #handleEndOfStream(EndOfStream) handleEndOfStream()}, 
     * {@link #handleEnd()}, and {@link #handleData(Object) handleDataMessage()}.</p>
     * 
     * <p>This method is responsible calling the more specific message and event handlers, and for
     * initializing the list of listeners using the listener configurations assigned
     * via the {@link #listeners(List) listeners()} method.</p>
     * 
     * <p>This method catches exceptions thrown by overridden message and event handlers.  If such
     * an exception is caught, the method reports the exception to the parent workflow via 
     * {@link #reportException(Exception) reportException()},
     * then stops the actor with a call to {@link #endStreamAndStop()} .</p>
     * 
     * @param message The received message.
     * @throws Exception if any of the other message and event handlers throw one.
     */
    @Override
    public final void onReceive(Object message) throws Exception {

        try {
            
            // handle control messages (subclasses of ControlMessage)
            if (message instanceof ControlMessage) {
                
                if (message instanceof Initialize) {
                
                    // compose the list of listeners from the configured list of listener configurations
                    for (ActorConfig listenerConfig : listenerConfigs) {
                        ActorRef listener = runner.getActorForConfig(listenerConfig);
                        listeners.add(listener);
                    }
                                
                    // invoke the Initialize event handler
                    handleInitialize();

                    // send a reply to this message
                    getSender().tell(new Response(), getSelf());
                    
                } else if (message instanceof Start) {
                    
                    // invoke the Start event handler
                    handleStart();
                
                } else if (message instanceof EndOfStream) {
                    
                    // invoke the EndOfStream message handler
                    handleEndOfStream((EndOfStream)message);
                }            
                
            // allow child classes to handle non-control messages
            } else {
                handleData(message);
            }
            
        } catch (Exception e) {
            reportException(e);
            endStreamAndStop();
        }
    }
    
    /** 
     * Empty default handler for <i>Initialize</i> event.  
     * Called when the actor receives a {@link org.kurator.akka.messages.Initialize Initialize} message.
     * 
     * <p>This method can be overridden by child classes to perform any tasks that must occur before 
     * the workflow begins to execute.  A workflow begins executing (and a {@link org.kurator.akka.messages.Start Start} message
     * is sent to each actor) only after <i>all</i> actors in the workflow receive the 
     * {@link org.kurator.akka.messages.Initialize Initialize} message and return from this handler.</p>
     * 
     * @throws Exception
     */
    protected void handleInitialize() throws Exception {}

    
    /** 
     * Empty default handler for <i>Start</i> event.  
     * Called when the actor receives a {@link org.kurator.akka.messages.Start Start} message.
     * 
     * <p> Can be overridden by children classes to perform any tasks that must occur once at the beginning 
     * of a workflow run but after all actors have been initialized. Actors that handle the
     * {@link org.kurator.akka.messages.Start Start}
     * message can bootstrap the execution of a workflow by peforming computations and sending one or more
     * messages before receiving messages from other actors in the workflow.</p>
     * 
     * <p> Note that if an actor is a listener of another actor in the workflow then it is <i>not</i> guaranteed 
     * to receive the {@link org.kurator.akka.messages.Start Start} message before receiving messages from other actors. 
     * This method thus is most useful when <b>(a)</b> an actor is not a listener, or <b>(b)</b> when the actor occurs 
     * in a workflow cycle such that the messages it receives are produced in response to the messages that it sends.</p>
     * 
     * <p> Note also that if an actor is not a listener of any other actor then it may delay returning from
     * this method until the actor has performed all of its activity for the workflow run. Thus, an actor serving
     * as a data source for a workflow may in some cases perform all of its work in an override of this method.
     * The {@link org.kurator.akka.actors.OneShot OneShot} actor provides a {@link org.kurator.akka.actors.OneShot#handleStart 
     * handleStart()} implementation that formalizes this approach.
     * </p>
     * 
     * @throws Exception
     */
    protected void handleStart() throws Exception {}
    
    
    /** 
     * Default handler for {@link org.kurator.akka.messages.EndOfStream EndOfStream} message. 
     * If the {@link #endOnEos} property is <i>true</i>, this method calls {@link #endStreamAndStop(EndOfStream)}.
     * 
     * <p> This method can be overridden by child classes to provide an alternative response to receiving 
     * an {@link org.kurator.akka.messages.EndOfStream EndOfStream} message. </p>
     * 
     * <p>If {@link #endOnEos}  is <i>true</I> and an actor simply needs to perform tasks before the
     * {@link org.kurator.akka.messages.EndOfStream EndOfStream} message is forwarded to listeners, 
     * the {@link #handleEnd handleEnd()}</code> method should be overridden instead.</p>
     * 
     * @param eos The received {@link org.kurator.akka.messages.EndOfStream EndOfStream} message.
     * @throws Exception
     */
    protected void handleEndOfStream(EndOfStream eos) throws Exception {
        if (endOnEos) {
            endStreamAndStop(eos);
        }
    }
    
    
    /** 
     * Empty default handler for <i>End</i> event.  Called when the actor has stopped sending 
     * messages to receivers and before the actor fully stops.
     *  
     * @throws Exception
     */
    protected void handleEnd() throws Exception {}
    
    
    /** 
     * Empty default handler for incoming data messages.  Called when the actor receives a message
     * that is not derived from {@link org.kurator.akka.messages.ControlMessage ControlMessage}.
     * 
     * <p>Most actors will override this method to receive incoming data from other actors.</p>
     *  
     * @param value The received data value.
     * @throws Exception
     */
    protected void handleData(Object value) throws Exception {}
    
    
    /** 
     * Sends a message to all of the the actor's listeners.
     * 
     * @param message The message to send.
     */
    protected final void broadcast(Object message) {
        for (ActorRef listener : listeners) {
            listener.tell(message, this.getSelf());
        }
    }    
        
    /** 
     * Stops the actor after (optionally) broadcasting the provided {@link org.kurator.akka.messages.EndOfStream EndOfStream} 
     * message to listeners.  It is called by {@link #handleEndOfStream(EndOfStream) handleEndOfStream()}
     * on arrival of an {@link org.kurator.akka.messages.EndOfStream EndOfStream} message if
     * the {@link #endOnEos} property is <i>true</i>.
     * 
     * <p> This method broadcasts the received {@link org.kurator.akka.messages.EndOfStream EndOfStream}
     * message (a new {@link org.kurator.akka.messages.EndOfStream EndOfStream} instance is created
     * if <code>eos</code> is <code>null</code>) to the actor's listeners if the {@link #sendEosOnEnd} 
     * property is <i>true</i>.
     * The method then calls {@link #handleEnd handleEnd()} and terminates the actor.
     * 
     * @param eos The {@link org.kurator.akka.messages.EndOfStream EndOfStream} message to broadcast to listeners.
     *            Can be <code>null</code> (see above).
     * @throws Exception if {@link #handleEnd handleEnd()} throws an exception.
     */
    protected final void endStreamAndStop(EndOfStream eos) throws Exception {
        
        // optionally send an EndOfStream message to listeners
        if (sendEosOnEnd) {
            broadcast(eos != null ? eos :  new EndOfStream());
        }
    
        // call the End event handler
        handleEnd();
        
        // stop the actor
        getContext().stop(getSelf());
    }
    
    
    /** 
     * Stops the actor after sending a new {@link org.kurator.akka.messages.EndOfStream EndOfStream} message to listeners.
     * 
     * <p>Calling this method is the primary means of shutting down an actor if {@link #endOnEos} is <i>false</i>.
     * </p>
     * 
     * @throws Exception if {@link #handleEnd handleEnd()} throws an exception.
     */
    protected final void endStreamAndStop() throws Exception {
        endStreamAndStop(null);
    }
    
    /** 
     * Used to report exceptions that are caught by this actor.  
     * 
     * <p>Child classes are expected to catch exceptions that occur while performing 
     * computations in response to incoming messages.  Exceptions that cannot be handled
     * silently should be reported via this method.</p>
     * 
     * <p>Exceptions that are not caught (or are thrown) by overridden message and event 
     * handlers are caught by {@link #onReceive(Object) onReceive()} which also reports the 
     * exception to the parent workflow via this method. However, any exception caught by 
     * {@link #onReceive(Object) onReceive()} also causes the actor to stop.</p>
     * 
     * @param exception The reported exception.
     */
    protected final void reportException(Exception exception) {
        ActorRef workflowRef = runner.getWorkflowRef();
        ExceptionMessage em = new ExceptionMessage(exception);
        workflowRef.tell(em, this.getSelf());
    }
}
