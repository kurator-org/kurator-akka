package org.kurator.akka.actors;

import java.io.PrintStream;

import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.Initialize;

public class PrintStreamWriter extends BroadcastActor {

    public PrintStream stream = System.out;
    public String separator = System.lineSeparator();
    
    private boolean isFirst = true;
    
    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof Initialize) {
        } else if (message instanceof EndOfStream) {
            broadcast(message);
            getContext().stop(getSelf());
        } else {
            if (isFirst) {
                isFirst = false;
            } else {
                stream.print(separator);
            }
            stream.print(message);
        }
    }
}
