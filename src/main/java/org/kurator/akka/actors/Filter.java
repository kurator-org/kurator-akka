package org.kurator.akka.actors;

import org.kurator.akka.messages.EndOfStream;

public class Filter extends Transformer {

    public int max = 1;
    public boolean sendEosOnExceed = false;

    private boolean eosSent = false;

    @Override
    public void handleDataMessage(Object message) throws Exception {

        if (eosSent)
            return;

        if (message instanceof Integer) {
            Integer value = (Integer) message;
            if (value <= max) {
                broadcast(value);
            } else if (sendEosOnExceed) {
                eosSent = true;
                broadcast(new EndOfStream());
            }
        }
    }
}
