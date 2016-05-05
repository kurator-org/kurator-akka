package org.kurator.akka.data;

public class WorkflowArtifact extends WorkflowProduct {

    public WorkflowArtifact(String actor, String type, String label, String artifact)  {
        super(actor, type, label, artifact);
    }
    
    @Override
    public String toString() {
        return String.format("{Actor:'%s', Type:'%s', Label:'%s', Artifact:'%s'}", actor, type, label, value); 
    }
}
