package org.kurator.akka.metadata;

public class MessageSendEvent implements MetadataItem {
    
    public final int actorId;
    public final int ordinal;
    
    public MessageSendEvent(int actorId, int ordinal) {
        this.actorId = actorId;
        this.ordinal = ordinal;
    }
}
