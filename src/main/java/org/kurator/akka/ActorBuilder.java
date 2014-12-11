package org.kurator.akka;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.KuratorAkkaActor;
import org.springframework.beans.factory.BeanNameAware;

public class ActorBuilder implements BeanNameAware {

    private Class<? extends KuratorAkkaActor> actorClass;
    private List<ActorBuilder> listeners;
    private Map<String,Object> defaults = new HashMap<String,Object>();
    private Map<String,Object> parameters = new HashMap<String,Object>();
    protected String actorName = null;

    public ActorBuilder() {
    }

    public ActorBuilder actorClass(Class<? extends KuratorAkkaActor> actorClass) {
        this.actorClass = actorClass;
        return this;
    }

    @Override
    public void setBeanName(String name) {
        actorName = name;
    }

    public ActorBuilder name(String name) {
        actorName = name;
        return this;
    }
    
    public String getName() {
        return actorName;
    }
    
    @SuppressWarnings("unchecked")
    public void setActorClass(String actorClassName) throws ClassNotFoundException {
        this.actorClass = (Class<? extends KuratorAkkaActor>) Class.forName(actorClassName);
    }
    
    public Class<? extends KuratorAkkaActor> actorClass() {
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
    
    public void setDefaults(Map<String, Object> defaults) {
        this.defaults = defaults;
    }
    
    public Map<String, Object> getDefaults() {
        return defaults;
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
