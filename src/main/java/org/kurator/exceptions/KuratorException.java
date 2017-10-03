package org.kurator.exceptions;

@SuppressWarnings("serial")
public class KuratorException extends Exception {
    
    public KuratorException(String message) {
        super(message);
    }
    
    public KuratorException(String message, Exception e) {
        super(message,e);
    }
}
