package org.kurator.akka.metadata;

import org.kurator.akka.AkkaActor;
import org.kurator.akka.messages.WrappedMessage;

public interface MetadataWriter {
    void writeMetadata(AkkaActor actor, WrappedMessage wrappedMessage);
}
