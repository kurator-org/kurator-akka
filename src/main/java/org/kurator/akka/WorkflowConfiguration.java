package org.kurator.akka;

import java.util.LinkedList;
import java.util.List;

public class WorkflowConfiguration {
    
    List<ActorConfiguration> actorConfigurations = new LinkedList<ActorConfiguration>();
    ActorConfiguration inputActorConfiguration;
    
    public void setActors(List<ActorConfiguration> actorConfigurations) {
        this.actorConfigurations = actorConfigurations;
    }
    
    public List<ActorConfiguration> getActors() {
        return actorConfigurations;
    }
    
    public void setInputActor(ActorConfiguration inputActor) {
        inputActorConfiguration = inputActor;
    }
    
    public ActorConfiguration getInputActor() {
        return inputActorConfiguration;
    }
}
