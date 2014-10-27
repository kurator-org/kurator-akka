package org.kurator.akka;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.BroadcastActor;

import akka.actor.IndirectActorProducer;

public class ActorProducer implements IndirectActorProducer {

    private Class<? extends BroadcastActor> actorClass;
    private Map<String, Object> parameters;
    private List<ActorBuilder> listenerBuilders;
    private WorkflowBuilder workflowBuilder;
    private BroadcastActor actor;

    public ActorProducer(Class<? extends BroadcastActor> actorClass, Map<String, Object> parameters, List<ActorBuilder> listenerConfigs, WorkflowBuilder workflowBuilder) {
        this.actorClass = actorClass;
        this.parameters = parameters;
        this.listenerBuilders = listenerConfigs;
        this.workflowBuilder = workflowBuilder;
    }

    @Override
    public Class<? extends BroadcastActor> actorClass() {
        return actorClass;
    }

    @Override
    public BroadcastActor produce() {
        
        try {
            actor = actorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        actor.setListenerConfigs(listenerBuilders);
        actor.setWorkflowRunner(workflowBuilder);
        
        if (parameters != null) {
            for (Map.Entry<String,Object> parameter : parameters.entrySet()) {
                setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        
        return actor;
    }
    
    
    private void setParameter(String name, Object value) {        
        try {
            Field field = actor.getClass().getField(name);
            field.setAccessible(true);
            field.set(actor, value);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } 
    }
}
