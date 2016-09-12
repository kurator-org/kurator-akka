package org.kurator;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * Base FSM strategy. Contains code ported from KuratorActor
 */
public abstract class FSMActorStrategy {
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
    protected Map<String,Object> settings;
    protected Map<String, Object> configuration;

    public abstract void onInitialize() throws Exception;
    public abstract void onStart();
    public abstract void onData(Object value);
    public abstract void onEnd();


}
