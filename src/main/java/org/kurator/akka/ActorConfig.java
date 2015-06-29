package org.kurator.akka;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;

public class ActorConfig implements BeanNameAware {

    public Class<? extends AkkaActor> actorClass;
    private List<ActorConfig> listeners;
    private Map<String,Object> defaults = new HashMap<String,Object>();
    private Map<String,Object> parameters = new HashMap<String,Object>();
    String actorName = null;
    private String code = null;
    private String script = null;
    private String onData = null;
    private String onStart = null;
    private String pythonClass = null;


    public ActorConfig() {
    }

    public ActorConfig actorClass(Class<? extends AkkaActor> actorClass) {
        this.actorClass = actorClass;
        return this;
    }

    @Override
    public void setBeanName(String name) {
        actorName = name;
    }

    public ActorConfig name(String name) {
        actorName = name;
        return this;
    }
    
    public String getName() {
        return actorName;
    }
    
    public void setOnStart(String onStart) {
        this.onStart = onStart;
    }
    
    public String getOnStart() {
        return onStart;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }

    public void setScript(String script) {
        this.script = script;
    }
    
    public String getScript() {
        return script;
    }

    public void setOnData(String onData) {
        this.onData = onData;
    }
    
    public String getOnData() {
        return onData;
    }

    public void setPythonClass(String pythonClass) {
        this.pythonClass = pythonClass;
    }
    
    public String getPythonClass() {
        return pythonClass;
    }
    
    @SuppressWarnings("unchecked")
    public void setActorClass(String actorClassName) throws ClassNotFoundException {
        this.actorClass = (Class<? extends AkkaActor>) Class.forName(actorClassName);
    }
    
    public Class<? extends AkkaActor> actorClass() {
        return actorClass;
    }
    
    public ActorConfig listener(ActorConfig listener) {
        if (listeners == null) {
            listeners = new LinkedList<ActorConfig>();
        }
        listeners.add(listener);
        return this;
    }

    public ActorConfig listensTo(ActorConfig sender) {
        sender.listener(this);
        return this;
    }
        
    public ActorConfig parameter(String parameter, Object value) {
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
    
    public void setListeners(List<ActorConfig> listeners) {
        this.listeners = listeners;
    }
    
    public List<ActorConfig> getListeners() {        
        return listeners;
    }
    
    public void setListensTo(List<ActorConfig> senders) {
        for (ActorConfig sender : senders) {
            sender.listener(this);
        }
    }
}
