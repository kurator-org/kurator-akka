package org.kurator.akka.messages;

import akka.actor.ActorRef;

/**
 * Created by lowery on 9/15/16.
 */
public class RegisterListener {
    public final ActorRef listener;

    public RegisterListener(ActorRef listener) {
        this.listener = listener;
    }
}
