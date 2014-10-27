package org.kurator.akka;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.BroadcastActor;

public class ActorBuilder {

    private Class<? extends BroadcastActor> actorClass;
    private List<ActorBuilder> listeners;
    private Map<String,Object> parameters;

    public ActorBuilder() {
    }

    public ActorBuilder actorClass(Class<? extends BroadcastActor> actorClass) {
        this.actorClass = actorClass;
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public void setActorClass(String actorClassName) throws ClassNotFoundException {
        this.actorClass = (Class<? extends BroadcastActor>) Class.forName(actorClassName);
    }
    
    public Class<? extends BroadcastActor> actorClass() {
        return actorClass;
    }
    
    public ActorBuilder listener(ActorBuilder listener) {
        if (listeners == null) {
            listeners = new LinkedList<ActorBuilder>();
        }
        listeners.add(listener);
        return this;
    }

    public ActorBuilder listensTo(ActorBuilder sender) {
        sender.listener(this);
        return this;
    }
        
    public ActorBuilder parameter(String parameter, Object value) {
        if (parameters == null) {
            parameters = new HashMap<String,Object>();
        }
        parameters.put(parameter, value);
        return this;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setListeners(List<ActorBuilder> listeners) {
        this.listeners = listeners;
    }
    
    public List<ActorBuilder> getListeners() {        
        return listeners;
    }
    
    public void setListensTo(List<ActorBuilder> senders) {
        for (ActorBuilder sender : senders) {
            sender.listener(this);
        }
    }

}
