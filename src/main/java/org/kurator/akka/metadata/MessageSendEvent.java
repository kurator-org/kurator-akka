package org.kurator.akka.metadata;

import akka.actor.Actor;

public class MessageSendEvent implements MetadataItem {
    
    public final Actor actor;
    public final int ordinal;
    
    public MessageSendEvent(Actor actor, int ordinal) {
        this.actor = actor;
        this.ordinal = ordinal;
    }
}
