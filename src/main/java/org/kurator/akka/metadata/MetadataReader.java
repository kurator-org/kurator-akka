package org.kurator.akka.metadata;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.messages.WrappedMessage;

public interface MetadataReader {
    void readMetadata(KuratorActor actor, WrappedMessage wrappedMessage) throws Exception;
}
