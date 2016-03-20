package org.kurator.akka.metadata;

import java.util.List;

import org.kurator.akka.AkkaActor;
import org.kurator.akka.messages.WrappedMessage;

public class BroadcastEventCountChecker implements MetadataReader {

    int count = 0;
    
    @Override
    public void readMetadata(AkkaActor actor, WrappedMessage wrappedMessage) throws Exception {
        List<MetadataItem> metadata = wrappedMessage.getMetadata(MessageSendEvent.class);
        if (!metadata.isEmpty()) {
            for (MetadataItem mi : metadata) {
                MessageSendEvent messageSendEvent = (MessageSendEvent)mi;
//                System.out.println(messageSendEvent.ordinal);
                if (++count != messageSendEvent.ordinal) {
                    throw new Exception("Wrong count");
                }
            }
        }
    }
}
