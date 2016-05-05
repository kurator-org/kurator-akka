package org.kurator.akka.data;

public class WorkflowProduct {
    
    public final String actor;
    public final String type;
    public final String label;
    public final Object value;
    
    
    public WorkflowProduct(String actor, String type, String label, Object value) {
        this.actor = actor;
        this.type = type;
        this.label = label;
        this.value = value;
    }    
    
    @Override
    public String toString() {
        return String.format("{Actor:'%s', Type:'%s', Label:'%s', Product:'%s'}", actor, type, label, value); 
    }

}
