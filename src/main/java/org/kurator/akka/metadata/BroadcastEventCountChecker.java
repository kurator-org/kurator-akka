package org.kurator.akka.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.messages.WrappedMessage;

public class BroadcastEventCountChecker implements MetadataReader {

    Map<Integer,Integer> countForActor = new HashMap<Integer,Integer>();
    
    @Override
    public void readMetadata(KuratorActor actor, WrappedMessage wrappedMessage) throws Exception {

        List<MetadataItem> messageSendEvents = wrappedMessage.getMetadata(MessageSendEvent.class);        
        if (messageSendEvents.isEmpty()) return;
            
        for (MetadataItem metadataItem : messageSendEvents) {

            MessageSendEvent messageSendEvent = (MessageSendEvent)metadataItem;
            
            Integer messageCountForSender = countForActor.get(messageSendEvent.actorId);
            if (messageCountForSender == null) {
                messageCountForSender = 1;
            } else {
                messageCountForSender++;
            }
            
            
            countForActor.put(messageSendEvent.actorId, messageCountForSender);
            
            if (messageCountForSender != messageSendEvent.ordinal) {
                System.out.println("Actor " + actor.id + " got message " + messageCountForSender + "(" + messageSendEvent.ordinal + ")");
                throw new Exception("Message between actors lost!");
            }
        }
    }
}
