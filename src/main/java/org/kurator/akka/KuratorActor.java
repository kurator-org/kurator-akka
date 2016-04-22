package org.kurator.akka;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.ExceptionMessage;
import org.kurator.akka.messages.Failure;
import org.kurator.akka.messages.FutureComplete;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Success;
import org.kurator.akka.messages.Start;
import org.kurator.akka.messages.WrappedMessage;
import org.kurator.akka.metadata.BroadcastEventCountChecker;
import org.kurator.akka.metadata.BroadcastEventCounter;
import org.kurator.akka.metadata.MetadataWriter;
import org.kurator.akka.metadata.MetadataReader;
import org.kurator.log.Log;
import org.kurator.log.Logger;
import org.kurator.log.SilentLogger;

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
public abstract class KuratorActor extends UntypedActor {
    
    /** Shorthand for platform-specific end-of-line character sequence. */
    public static final String EOL = System.getProperty("line.separator");
    
    private static Integer nextActorId = 1;

    /** Determines if this actor automatically terminates when it receives an 
     * {@link org.kurator.akka.messages.EndOfStream EndOfStream} message.
     * Defaults to <i>true</i>. */
    public boolean endOnEos = true;

    /** Determines if this actor automatically sends an 
     * {@link org.kurator.akka.messages.EndOfStream EndOfStream} message to
     * all of its listeners just before it stops executing.
     * Defaults to <i>true</i>. */
    public boolean sendEosOnEnd = true;
    
    public boolean needsTrigger = false;
    
    /** Stream used by actor instead of reading from <code>System.in</code> directly. 
     * Defaults to <code>System.in</code>. 
     * <p>Non-default value assigned can be assigned via the {@link #inputStream inputStream()} method.</p>
     */
    protected volatile InputStream inStream = System.in;
    
    /** Stream used by actor instead of writing to <code>System.out</code> directly. 
     * Defaults to <code>System.out</code>. 
     * <p>Non-default value assigned can be assigned via the {@link #outputStream outputStream()} method.</p>
     */
    protected volatile PrintStream outStream = System.out;

    /** Stream used by actor instead of writing to <code>System.err</code> directly. 
     * Defaults to <code>System.err</code>. 
     * <p>Non-default value can be assigned via the {@link #errorStream errorStream()} method.</p>
     */
    protected volatile PrintStream errStream = System.err;

    // private fields
    public final int id;
    private List<ActorConfig> listenerConfigs = new LinkedList<ActorConfig>();
    private Set<ActorRef> listeners = new HashSet<ActorRef>();
    private WorkflowRunner runner;
    protected Map<String, String> inputs = new HashMap<String,String>();
    protected String name;
    protected Map<String,Object> settings;
    protected Map<String, Object> configuration;
    protected ActorFSM state = ActorFSM.CONSTRUCTED;
    private List<MetadataWriter> metadataWriters = null;
    private List<MetadataReader> metadataReaders = null;
    protected Logger logger = new SilentLogger();

    private WrappedMessage receivedWrappedMessage;
    
    private enum ActorFSM {
        CONSTRUCTED,
        BUILT,
        INITIALIZED,
        STARTED,
        ENDED
    }
    
    
    public KuratorActor() {
        
        synchronized(nextActorId) {
            this.id = nextActorId++;
        }
        
        this.metadataReaders = new LinkedList<MetadataReader>();
        this.metadataReaders.add(new BroadcastEventCountChecker());

        this.metadataWriters = new LinkedList<MetadataWriter>();
        this.metadataWriters.add(new BroadcastEventCounter());
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
    public synchronized KuratorActor errorStream(PrintStream errStream) {
        this.errStream = errStream;
        return this;
    }

    /** 
     * Specifies the input stream to be used by an actor that needs
     * to read from <code>stdin</code>. The value is stored in {@link #inStream}.
     * 
     * <p>Child classes should read input from {@link #inStream} instead of 
     * reading from <code>System.in</code> directly.</p>
     * 
     * @param inStream The InputStream to use for reading from <code>stdin</code>.
     * @return this AkkaActor
     */
    public synchronized KuratorActor inputStream(InputStream inStream) {
        this.inStream = inStream;
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
    public synchronized KuratorActor outputStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }
    
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
    public synchronized KuratorActor listeners(List<ActorConfig> listenerConfigs) {
        Contract.requires(state, ActorFSM.CONSTRUCTED);
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
    public synchronized KuratorActor runner(WorkflowRunner runner) {
        Contract.requires(state, ActorFSM.CONSTRUCTED);
        this.runner = runner;
        return this;
    }

    public synchronized void settings(Map<String, Object> settings) {
        Contract.requires(state, ActorFSM.CONSTRUCTED);
        this.settings = settings;
    }

    public synchronized KuratorActor inputs(Map<String,String> inputs) {
        this.inputs = inputs;
        return this;
    }
    
    public synchronized KuratorActor setNeedsTrigger(boolean needsTrigger) {
        Contract.requires(state, ActorFSM.CONSTRUCTED);
        this.needsTrigger = needsTrigger;
        return this;
    }
    
    public synchronized KuratorActor metadataWriters(List<MetadataWriter> metadataWriters) {
        if (this.metadataWriters == null) {
            this.metadataWriters = metadataWriters;
        } else {
            this.metadataWriters.addAll(metadataWriters);
        }
        return this;
    }

    public synchronized KuratorActor metadataReaders(List<MetadataReader> metadataReaders) {
        if (this.metadataReaders == null) {
            this.metadataReaders = metadataReaders;
        } else {
            this.metadataReaders.addAll(metadataReaders);
        }
        return this;
    }

    public synchronized KuratorActor configuration(Map<String, Object> configuration) {
        Contract.requires(state, ActorFSM.CONSTRUCTED);
        this.configuration = configuration;
        return this;
    }
        
    /** 
     * Initial handler for all messages received by this actor via the Akka framework.  
     * 
     * <p> This method may not be overridden by child classes.  Non-default responses to
     * messages can provided by overriding one or more of {@link #onInitialize()}, 
     * {@link #onStart()}, {@link #onEndOfStream(EndOfStream) handleEndOfStream()}, 
     * {@link #onEnd()}, and {@link #onData(Object) handleDataMessage()}.</p>
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
    public synchronized final void onReceive(Object message) throws Exception {

        Contract.disallows(state, ActorFSM.ENDED);

        try {

            if (message instanceof WrappedMessage) {
                receivedWrappedMessage = (WrappedMessage)message;
                message = unwrapMessage(receivedWrappedMessage);
            } else {
                receivedWrappedMessage = null;
            }
            
            // handle control messages (subclasses of ControlMessage)
            if (message instanceof ControlMessage) {
                
                if (message instanceof Initialize) {
                    
                    name = (String) configuration.get("name");
                    this.logger.setSource(Log.ACTOR(name));

                    logger.comm("Received INITIALIZE message from WORKFLOW");
                    
                    Contract.requires(state, ActorFSM.CONSTRUCTED);

                    // compose the list of listeners from the configured list of listener configurations
                    for (ActorConfig listenerConfig : listenerConfigs) {
                        ActorRef listener = runner.getActorForConfig(listenerConfig);
                        listeners.add(listener);
                    }

                    // invoke the Initialize event handler
                    try {
                        logger.trace("Invoking ON_INITIALIZE_EVENT handler");
                        onInitialize();
                    } catch(Exception initializationException) {
                        
                        Object exceptionMessage = initializationException.getMessage();
                        if (exceptionMessage == null || ((String)exceptionMessage).isEmpty()) {
                            exceptionMessage = initializationException.toString();
                        }
                                
                        List<Failure> failures = new LinkedList<Failure>();
                        failures.add(new Failure("Error initializing actor '" + name + "'"));

                        failures.add(new Failure(exceptionMessage.toString()));
                        getSender().tell(new Failure(failures), getSelf());
                        getContext().stop(getSelf());
                        return;
                    }
                    
                    state = ActorFSM.INITIALIZED;
                    
                    // report success
                    logger.comm("Sending INITIALIZE response to WORKFLOW");
                    getSender().tell(new Success(), getSelf());
                    
                } else if (message instanceof Start) {
                    
                    logger.comm("Received START message from WORKFLOW");

                    Contract.requires(state, ActorFSM.INITIALIZED, ActorFSM.STARTED);

                    if (state == ActorFSM.INITIALIZED) {
                        state = ActorFSM.STARTED;
                        logger.trace("Invoking ON_START_EVENT handler");
                        handleOnStart();
                    }
                
                } else if (message instanceof EndOfStream) {
                    logger.comm("Received END_OF_STREAM message from " + Log.ACTOR(runner.name(getSender())));
                    Contract.requires(state, ActorFSM.INITIALIZED, ActorFSM.STARTED);
                    logger.trace("Invoking ON_END_OF_STREAM_EVENT handler");
                    onEndOfStream((EndOfStream)message);
                    
                } else if (message instanceof FutureComplete) {
                    Contract.requires(state, ActorFSM.INITIALIZED, ActorFSM.STARTED);
                    onFutureComplete((FutureComplete)message);
                }
                
            // all other messages are assumed to be data
            } else {
                
                logger.comm("Received DATA from " + Log.ACTOR(runner.name(getSender())));
                logger.value("Received DATA", message);

                Contract.requires(state, ActorFSM.STARTED, ActorFSM.INITIALIZED);
                
                if (state == ActorFSM.INITIALIZED) {
                    logger.trace("Invoking ON_START_EVENT handler");
                    state = ActorFSM.STARTED;
                    handleOnStart();
                }
                
                logger.trace("Invoking ON_DATA_EVENT handler");
                onData(message);
            }
            
        } catch (Exception e) {
            reportException(e);
            errStream.println(e);
            endStreamAndStop();
        }
    }

    private void handleOnStart() throws Exception {
        onStart();
        if (this.needsTrigger) {
            logger.trace("Invoking ON_TRIGGER_EVENT handler");
            onTrigger();
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
     * @throws Exception If the actor implementation of onInitialize() method throws an exception.
     */
    protected void onInitialize() throws Exception {
        logger.trace("Executing default ON_INITIALIZE_EVENT handler");
    }

    
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
     * The {@link org.kurator.akka.actors.OneShot OneShot} actor provides a {@link org.kurator.akka.actors.OneShot#onStart 
     * onStart()} implementation that formalizes this approach.
     * </p>
     * 
     * @throws Exception If the actor implementation of onStart() method throws an exception.
     */
    protected void onStart() throws Exception {
        logger.trace("Executing default ON_START_EVENT handler");
    }
    
    protected void onTrigger() throws Exception {
        logger.trace("Executing default ON_TRIGGER_EVENT handler");
    }
    
    /** 
     * Default handler for {@link org.kurator.akka.messages.EndOfStream EndOfStream} message. 
     * If the {@link #endOnEos} property is <i>true</i>, this method calls {@link #endStreamAndStop(EndOfStream)}.
     * 
     * <p> This method can be overridden by child classes to provide an alternative response to receiving 
     * an {@link org.kurator.akka.messages.EndOfStream EndOfStream} message. </p>
     * 
     * <p>If {@link #endOnEos}  is <i>true</I> and an actor simply needs to perform tasks before the
     * {@link org.kurator.akka.messages.EndOfStream EndOfStream} message is forwarded to listeners, 
     * the <code>{@link #onEnd onEnd()}</code> method should be overridden instead.</p>
     * 
     * @param eos The received {@link org.kurator.akka.messages.EndOfStream EndOfStream} message.
     * @throws Exception If the actor implementation of onTrigger() method throws and exception.
     */
    protected void onEndOfStream(EndOfStream eos) throws Exception {
        logger.trace("Executing default ON_END_OF_STREAM_EVENT handler");
        if (endOnEos) {
            endStreamAndStop(eos);
        }
    }
    
    
    /** 
     * Empty default handler for <i>End</i> event.  Called when the actor has stopped sending 
     * messages to receivers and before the actor fully stops.
     *  
     * @throws Exception If the actor implementation of onEnd() method throws an exception.
     */
    protected void onEnd() throws Exception {
        logger.trace("Executing default ON_END_EVENT handler");
    }

    protected void onFutureComplete(FutureComplete message) throws Exception { }
    
    /** 
     * Empty default handler for incoming data messages.  Called when the actor receives a message
     * that is not derived from {@link org.kurator.akka.messages.ControlMessage ControlMessage}.
     * 
     * <p>Most actors will override this method to receive incoming data from other actors.</p>
     *  
     * @param value The received data value.
     * @throws Exception If the actor implementation of onEnd() method throws an exception.
     */
    protected void onData(Object value) throws Exception {
        logger.trace("Executing default ON_DATA_EVENT handler");
    }
    
    
    private Object wrapMessage(Object message) {
        if (metadataWriters == null) {
            return message;
        } else {
            WrappedMessage wrappedMessage = new WrappedMessage(message);
            for (MetadataWriter mw : metadataWriters) {
                mw.writeMetadata(this, wrappedMessage);
            }
            return wrappedMessage;
        }
    }
        
    private Object unwrapMessage(WrappedMessage wrappedMessage) throws Exception {
        if (metadataReaders != null) {
            for (MetadataReader mr : metadataReaders) {
                mr.readMetadata(this, wrappedMessage);
            }
        }
        return wrappedMessage.unwrap();
    }

    public synchronized WrappedMessage getReceivedWrappedMessage() {
        return receivedWrappedMessage;
    }
    
    /** 
     * Sends a message to all of the the actor's listeners.
     * 
     * @param message The message to send.
     */
    protected synchronized final void broadcast(Object message) {
        
        Contract.requires(state, ActorFSM.INITIALIZED, ActorFSM.STARTED);
        
        Object wrappedMessage = wrapMessage(message);
        
        for (ActorRef listener : listeners) {
            if (listener != null) {
                logger.comm("Sending DATA to " + Log.ACTOR(runner.name(listener)));
                listener.tell(wrappedMessage, this.getSelf());
                logger.value("Sent DATA",  message);
            }
        }
    }    
        
    /** 
     * Stops the actor after (optionally) broadcasting the provided {@link org.kurator.akka.messages.EndOfStream EndOfStream} 
     * message to listeners.  It is called by {@link #onEndOfStream(EndOfStream) handleEndOfStream()}
     * on arrival of an {@link org.kurator.akka.messages.EndOfStream EndOfStream} message if
     * the {@link #endOnEos} property is <i>true</i>.
     * 
     * <p> This method broadcasts the received {@link org.kurator.akka.messages.EndOfStream EndOfStream}
     * message (a new {@link org.kurator.akka.messages.EndOfStream EndOfStream} instance is created
     * if <code>eos</code> is <code>null</code>) to the actor's listeners if the {@link #sendEosOnEnd} 
     * property is <i>true</i>.
     * The method then calls {@link #onEnd onEnd()} and terminates the actor.
     * 
     * @param eos The {@link org.kurator.akka.messages.EndOfStream EndOfStream} message to broadcast to listeners.
     *            Can be <code>null</code> (see above).
     * @throws Exception if {@link #onEnd onEnd()} throws an exception.
     */
    protected final void endStreamAndStop(EndOfStream eos) throws Exception {
        
        // optionally send an EndOfStream message to listeners
        if (sendEosOnEnd) {
            broadcast(eos != null ? eos :  new EndOfStream());
        }
    
        // call the End event handler
        onEnd();
        
        // stop the actor
        getContext().stop(getSelf());
        
        state = ActorFSM.ENDED;
    }
    
    
    /** 
     * Stops the actor after sending a new {@link org.kurator.akka.messages.EndOfStream EndOfStream} message to listeners.
     * 
     * <p>Calling this method is the primary means of shutting down an actor if {@link #endOnEos} is <i>false</i>.
     * </p>
     * 
     * @throws Exception if {@link #onEnd onEnd()} throws an exception.
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
//        exception.printStackTrace();
        ActorRef workflowRef = runner.getWorkflowRef();
        ExceptionMessage em = new ExceptionMessage(exception);
        workflowRef.tell(em, this.getSelf());
    }

    public KuratorActor logger(Logger logger) {
        this.logger = logger;
        return this;
    }
}
