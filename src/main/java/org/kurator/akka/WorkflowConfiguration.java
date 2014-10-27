package org.kurator.akka;

import java.util.LinkedList;
import java.util.List;

public class WorkflowConfiguration {
    
    List<ActorBuilder> actorConfigurations = new LinkedList<ActorBuilder>();
    ActorBuilder inputActorConfiguration;
    
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
}
