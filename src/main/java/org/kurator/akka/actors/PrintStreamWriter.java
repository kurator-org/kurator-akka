package org.kurator.akka.actors;

import java.io.PrintStream;

import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.Initialize;

public class PrintStreamWriter extends BroadcastActor {

    public PrintStream stream = System.out;
    public String separator = System.lineSeparator();
    
    private boolean isFirst = true;
    
    @Override
    public void onReceive(Object message) throws Exception {
        
        super.onReceive(message);
        
        if (message instanceof ControlMessage) {
            if (message instanceof EndOfStream) {
                broadcast(message);
                getContext().stop(getSelf());
            }
            return;
        }
        
        if (isFirst) {
            isFirst = false;
        } else {
            stream.print(separator);
        }
        stream.print(message);
    }
}
