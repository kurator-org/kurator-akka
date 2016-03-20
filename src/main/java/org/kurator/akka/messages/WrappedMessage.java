package org.kurator.akka.messages;

import java.util.LinkedList;
import java.util.List;

import org.kurator.akka.metadata.MetadataItem;

public class WrappedMessage  {

    private Object message;
    protected List<MetadataItem> metadata;
    
    public WrappedMessage(Object message) {
        this.message = message;
        metadata = new LinkedList<MetadataItem>();
    }

    public WrappedMessage(Object message, List<MetadataItem> metadata) {
        this.message = message;
        this.metadata = new LinkedList<MetadataItem>(metadata);
    }
    
    public Object unwrap() {
        return message;
    }

    public void addMetadata(MetadataItem metadataItem) {
        this.metadata.add(metadataItem);
    }

    public void addMetadata(List<MetadataItem> metadata) {
        metadata.addAll(metadata);
    }

    public List<MetadataItem> getMetadata() {
        return new LinkedList<MetadataItem>(metadata);
    }

    
    public List<MetadataItem> getMetadata(Class<? extends MetadataItem> metadataType) {
        List<MetadataItem> matchingMetadata = new LinkedList<MetadataItem>();
        for (MetadataItem m : this.metadata) {
            if (metadataType.isInstance(m)) {
                matchingMetadata.add(m);
            }
        }
        return matchingMetadata;
    }
}
