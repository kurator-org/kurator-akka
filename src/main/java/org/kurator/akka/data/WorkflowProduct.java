package org.kurator.akka.data;

public class WorkflowProduct {
    
    public final String actor;
    public final String label;
    public final Object value;
    
    public WorkflowProduct(String actorName, String productLabel, Object value) {
        this.actor = actorName;
        this.label = productLabel;
        this.value = value;
    }    
    
    @Override
    public String toString() {
        return String.format("{Actor:'%s', Label:'%s', Artifact:'%s'}", actor, label, value); 
    }

}
