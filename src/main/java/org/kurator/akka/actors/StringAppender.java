package org.kurator.akka.actors;

public class StringAppender extends AkkaActor {

    public String suffix = "";

    @Override
    public void handleData(Object value) throws Exception {
        if (value instanceof String) {
            broadcast((String)value + suffix);
        }
    }
}
