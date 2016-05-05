package org.kurator.akka.data;

import org.kurator.akka.KuratorActor;

public class ActorFileProduct extends ActorProduct {

    public ActorFileProduct(KuratorActor publisher, String label, String filePath) {
        super(publisher, label, filePath);
    }
}
