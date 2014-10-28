package org.kurator.akka;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkflowConfiguration {
    
    List<ActorBuilder> actorConfigurations = new LinkedList<ActorBuilder>();
    ActorBuilder inputActorConfiguration;
    Map<String,Object> settings;
    
    public void setActors(List<ActorBuilder> actorConfigurations) {
        this.actorConfigurations = actorConfigurations;
    }
    
    public List<ActorBuilder> getActors() {
        return actorConfigurations;
    }
    
    public void setInputActor(ActorBuilder inputActor) {
        inputActorConfiguration = inputActor;
    }
    
    public ActorBuilder getInputActor() {
        return inputActorConfiguration;
    }
    
    public void setParameters(Map<String,Object> settings) {
        this.settings = settings;
    }
    
    public Map<String,Object> getParameters() { 
        return settings;
    }
}
