package org.kurator.akka.metadata;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.messages.WrappedMessage;

public interface MetadataWriter {
    void writeMetadata(KuratorActor actor, WrappedMessage wrappedMessage);
}
