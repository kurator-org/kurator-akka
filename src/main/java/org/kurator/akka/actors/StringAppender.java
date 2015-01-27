package org.kurator.akka.actors;

public class StringAppender extends AkkaActor {

    public String suffix = "";

    @Override
    public void handleDataMessage(Object message) throws Exception {
        if (message instanceof String) {
            broadcast((String)message + suffix);
        }
    }
}
