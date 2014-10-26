package org.kurator.akka;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.BroadcastActor;

import akka.actor.IndirectActorProducer;

public class ActorBuilder implements IndirectActorProducer {

    private Class<? extends BroadcastActor> actorClass;
    private Map<String, Object> parameters;
    private List<ActorConfiguration> listenerConfigs;
    private WorkflowRunner workflowRunner;
    private BroadcastActor actor;

    public ActorBuilder(Class<? extends BroadcastActor> actorClass, Map<String, Object> parameters, List<ActorConfiguration> listenerConfigs, WorkflowRunner runner) {
        this.actorClass = actorClass;
        this.parameters = parameters;
        this.listenerConfigs = listenerConfigs;
        this.workflowRunner = runner;
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
        actor.setListenerConfigs(listenerConfigs);
        actor.setWorkflowRunner(workflowRunner);
        
        if (parameters != null) {
            for (Map.Entry<String,Object> parameter : parameters.entrySet()) {
                setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        
        return actor;
    }
    
    
    /** Uses reflection to set the named property on the bean to the provided value */
    private void setParameter(String name, Object value) {        
        Field field;
        try {
            field = actor.getClass().getField(name);
            field.setAccessible(true);
            field.set(actor, value);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
