package org.kurator.akka;

import java.io.PrintStream;

class PrintStreamWriter extends BroadcastActor {

    private PrintStream stream;

    public PrintStreamWriter(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof Initialize) {
        } else if (message instanceof EndOfStream) {
            broadcast(message);
            getContext().stop(getSelf());
        } else {
            stream.println(message);
        }
    }
}
