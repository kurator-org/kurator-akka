package org.kurator.akka.data;

import org.kurator.akka.KuratorActor;

public class ActorProduct {
    
    public final KuratorActor publisher;
    public final String label;
    public final Object artifact;

    public ActorProduct(KuratorActor publisher, String label, Object artifact) {
        this.publisher = publisher;
        this.label = label;
        this.artifact = artifact;
    }
    
    @Override
    public String toString() {
        return publisher + ":" + label + ":" + artifact; 
    }
}
