package org.kurator.akka.metadata;

import org.kurator.akka.AkkaActor;
import org.kurator.akka.messages.WrappedMessage;

public interface MetadataReader {
    void readMetadata(AkkaActor actor, WrappedMessage wrappedMessage) throws Exception;
}
