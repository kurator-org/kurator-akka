package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;
import org.kurator.akka.messages.EndOfStream;

public class Filter extends AkkaActor {

    public int max = 1;
    public boolean sendEosOnExceed = false;

    private boolean eosSent = false;

    @Override
    public void onData(Object value) throws Exception {

        if (eosSent)
            return;

        if (value instanceof Integer) {
            Integer intValue = (Integer) value;
            if (intValue <= max) {
                broadcast(intValue);
            } else if (sendEosOnExceed) {
                eosSent = true;
                broadcast(new EndOfStream());
            }
        }
    }
}
