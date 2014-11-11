package org.kurator.akka.actors;

import java.io.PrintStream;

public class PrintStreamWriter extends Transformer {

    public PrintStream stream = System.out;
    public String separator = System.lineSeparator();
    
    private boolean isFirst = true;
    
    @Override
    public void handleDataMessage(Object message) throws Exception {
        
        if (isFirst) {
            isFirst = false;
        } else {
            stream.print(separator);
        }
        stream.print(message);
    }
}
