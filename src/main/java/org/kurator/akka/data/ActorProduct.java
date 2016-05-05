package org.kurator.akka.data;

public class ActorProduct {
    
    public final String publisher;
    public final String label;
    public final Object product;

    public ActorProduct(String actorName, String productLabel, Object product) {
        this.publisher = actorName;
        this.label = productLabel;
        this.product = product;
    }
    
    @Override
    public String toString() {
        return String.format("{Actor:'%s', Label:'%s', Product:'%s'}", publisher, label, product); 
    }
}
