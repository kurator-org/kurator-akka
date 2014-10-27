package org.kurator.akka;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.BroadcastActor;

public class ActorConfiguration {

    private Class<? extends BroadcastActor> actorClass;
    private List<ActorConfiguration> listeners;
    private Map<String,Object> parameters;

    public ActorConfiguration() {
    }

    public static ActorConfiguration create() {
        return new ActorConfiguration();
    }
    
    public ActorConfiguration actorClass(Class<? extends BroadcastActor> actorClass) {
        this.actorClass = actorClass;
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public void setActorClassName(String actorClassName) throws ClassNotFoundException {
        this.actorClass = (Class<? extends BroadcastActor>) Class.forName(actorClassName);
    }
    
    public Class<? extends BroadcastActor> getActorClass() {
        return actorClass;
    }
    
    public ActorConfiguration listener(ActorConfiguration listener) {
        if (listeners == null) {
            listeners = new LinkedList<ActorConfiguration>();
        }
        listeners.add(listener);
        return this;
    }

    public ActorConfiguration listensTo(ActorConfiguration sender) {
        sender.listener(this);
        return this;
    }
    
    public ActorConfiguration parameter(String parameter, Object value) {
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
    
    public void setListeners(List<ActorConfiguration> listeners) {
        this.listeners = listeners;
    }
    
    public List<ActorConfiguration> getListeners() {
        return listeners;
    }
}
