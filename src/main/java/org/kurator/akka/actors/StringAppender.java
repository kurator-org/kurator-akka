package org.kurator.akka.actors;

import org.kurator.akka.KuratorActor;

public class StringAppender extends KuratorActor {

    public String suffix = "";

    @Override
    public void onData(Object value) throws Exception {
        if (value instanceof String) {
            broadcast((String)value + suffix);
        }
    }
}
