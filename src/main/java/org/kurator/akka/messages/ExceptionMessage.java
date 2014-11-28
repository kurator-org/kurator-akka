package org.kurator.akka.messages;

public class ExceptionMessage implements ControlMessage {

    private final Exception exception;
    
    public ExceptionMessage(Exception e) {
        exception = e;
    }
    
    public Exception getException() {
        return exception;
    }
    
}
