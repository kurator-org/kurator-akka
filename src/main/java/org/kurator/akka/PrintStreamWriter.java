package org.kurator.akka;

import java.io.PrintStream;

class PrintStreamWriter extends BroadcastActor {

    private PrintStream stream;
    private String separator;
    private boolean isFirst = true;

    public PrintStreamWriter(PrintStream stream, String separator) {
        this.stream = stream;
        this.separator = separator;
    }

    public PrintStreamWriter(PrintStream stream) {
        this(stream, System.lineSeparator());
    }

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
