package org.kurator.akka.messages;

import java.util.LinkedList;
import java.util.List;

import org.kurator.akka.data.SpacedStringBuilder;

public class Failure implements ControlMessage {
    
    public final String message;
    public static final String EOL = System.getProperty("line.separator");
    public final List<Failure> failures = new LinkedList<Failure>();
    
    public Failure() { 
        message = null;
    }

    public Failure(String message) { 
        this.message = message;
    }
    
    public Failure(List<Failure> failures) {
        this(null, failures);
    }

    public Failure(String message, List<Failure> failures) {
        this.message = message; 
        this.failures.addAll(failures);
    }
        
    public String toString() {
        
        SpacedStringBuilder errorMessage = new SpacedStringBuilder(EOL);
        
        if (message != null) {
            errorMessage.append(message);
        }
        
        for (Failure f : failures) {
            errorMessage.append(f.toString());
        }
        
        return errorMessage.toString();
    }
}
