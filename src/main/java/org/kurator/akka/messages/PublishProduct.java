package org.kurator.akka.messages;

import org.kurator.akka.data.ActorProduct;

public class PublishProduct {

    public final ActorProduct product;
        	
    public PublishProduct(ActorProduct product) {
        this.product = product;
	}
}
