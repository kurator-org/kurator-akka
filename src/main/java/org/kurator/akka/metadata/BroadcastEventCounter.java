package org.kurator.akka.metadata;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.messages.WrappedMessage;

public class BroadcastEventCounter implements MetadataWriter {

    private int eventCount = 0;
    
    @Override
    public void writeMetadata(KuratorActor actor, WrappedMessage wrappedMessage) {
        eventCount += 1;
        MetadataItem m = new MessageSendEvent(actor.id, eventCount);
        wrappedMessage.addMetadata(m);
    }
}
