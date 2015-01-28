package org.kurator.akka;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkflowConfiguration {
    
    List<ActorConfig> actors = new LinkedList<ActorConfig>();
    ActorConfig inputActor;
    Map<String,Object> settings;
    
    public void setActors(List<ActorConfig> actors) {
        this.actors = actors;
    }
    
    public List<ActorConfig> getActors() {
        return actors;
    }
    
    public void setInputActor(ActorConfig inputActor) {
        this.inputActor = inputActor;
    }
    
    public ActorConfig getInputActor() {
        return inputActor;
    }
    
    public void setParameters(Map<String,Object> settings) {
        this.settings = settings;
    }
    
    public Map<String,Object> getParameters() { 
        return settings;
    }
}
