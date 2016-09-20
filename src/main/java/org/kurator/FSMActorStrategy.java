package org.kurator;

import akka.actor.ActorRef;
import org.kurator.akka.Contract;
import org.kurator.akka.data.WorkflowArtifact;
import org.kurator.akka.data.WorkflowProduct;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.ProductPublication;
import org.kurator.log.Log;
import org.kurator.log.Logger;
import org.kurator.log.SilentLogger;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Base FSM strategy. Contains code ported from KuratorActor
 */
public abstract class FSMActorStrategy {
    protected Logger logger = new SilentLogger();

    /** Shorthand for platform-specific end-of-line character sequence. */
    public static final String EOL = System.getProperty("line.separator");


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

    protected String name;
    protected Map<String, String> inputs = new HashMap<String,String>();
    protected Map<String,Object> settings;
    protected Map<String, Object> configuration;

    public abstract void onInitialize() throws Exception;
    public abstract void onStart() throws Exception;
    public abstract Object onData(Object value) throws Exception;
    public abstract void onEnd() throws Exception;

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
        // call the End event handler
        onEnd();
    }

    /**
     * Sends a message to all of the the actor's listeners.
     *
     * @param message The message to send.
     */
    protected synchronized final void broadcast(Object message) {
        // TODO: add implementation
    }

    protected void publishProducts(Map<String,Object> products) {
        if (products != null ) {
            logger.debug("Publishing " + products.size() + " products.");
            for(Map.Entry<String, Object> entry: products.entrySet()) {
                String label = (String) entry.getKey();
                Object product = entry.getValue();
                publishProduct(label, product);
            }
            logger.trace("Done publishing products");
        }
    }

    protected void publishProduct(String label, Object product) {
        publishProduct(label, product, product.getClass().getName());
    }

    protected void publishProduct(String label, Object product, String type) {
        WorkflowProduct ap = new WorkflowProduct(this.name, type, label, product);
        logger.value("Published product:", label, product);
        ProductPublication message = new ProductPublication(ap);
        //logger.trace("Sending product to " + workflowRef);
        logger.comm("Sending value PUBLICATION_REQUEST message to WORKFLOW");
       // workflowRef.tell(message, getSelf());
    }

    protected void publishArtifacts(Map<String,String> artifacts) {
        if (artifacts != null ) {
            logger.debug("Publishing " + artifacts.size() + " artifacts.");
            for(Map.Entry<String, String> entry: artifacts.entrySet()) {
                String label = (String) entry.getKey();
                String artifact = (String)entry.getValue();
                publishArtifact(label, artifact);
            }
            logger.trace("Done publishing artifacts");
        }
    }

    protected void publishArtifact(String label, String pathToArtifact) {
        publishProduct(label, pathToArtifact, "File");
    }

    protected ProductPublication publishArtifact(String label, String pathToArtifact, String type) {
        WorkflowArtifact ap = new WorkflowArtifact(this.name, type, label, pathToArtifact);
        logger.value("Published artifact:", label, pathToArtifact);
        ProductPublication message = new ProductPublication(ap);
        logger.comm("Sending artifact PUBLICATION_REQUEST message to WORKFLOW");

        return message;
    }
}
