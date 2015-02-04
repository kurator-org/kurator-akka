package org.kurator.akka;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.AkkaActor;

import akka.actor.IndirectActorProducer;

public class ActorProducer implements IndirectActorProducer {

    private Class<? extends AkkaActor> actorClass;
    private Map<String, Object> defaults;
    private Map<String, Object> parameters;
    private List<ActorConfig> listeners;
    private WorkflowRunner workflowRunner;
    private AkkaActor actor;
    private PrintStream outStream;
    private PrintStream errStream;

    public ActorProducer(Class<? extends AkkaActor> actorClass, Map<String, Object> defaults, Map<String, Object> parameters, List<ActorConfig> listeners, 
            PrintStream outStream, PrintStream errStream, WorkflowRunner workflowRunner) {
        this.actorClass = actorClass;
        this.defaults = defaults;
        this.parameters = parameters;
        this.listeners = listeners;
        this.workflowRunner = workflowRunner;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    @Override
    public Class<? extends AkkaActor> actorClass() {
        return actorClass;
    }

    @Override
    public AkkaActor produce() {
        
        // create the actor instance from its class
        try {
            actor = actorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        // configure the actor according to its configuration
        actor.listeners(listeners)
             .runner(workflowRunner)
             .outputStream(outStream)
             .errorStream(errStream);

        // assign values to the actor parameters 
        Map<String,Object> parameterSettings = new HashMap<String,Object>();
        Map<String,Object> unappliedSettings = new HashMap<String,Object>();
        parameterSettings.putAll(defaults);
        parameterSettings.putAll(parameters);
        for (Map.Entry<String,Object> setting : parameterSettings.entrySet()) {
            String name = setting.getKey();
            Object value = setting.getValue();
            boolean parameterWasSet = false;
            try {
                 parameterWasSet = (setParameter(name, value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            
            if (!parameterWasSet) {
                unappliedSettings.put(name, value);
            }
        }
        
        actor.settings(unappliedSettings);
            
        return actor;
    }
    
    
    private boolean setParameter(String name, Object value) throws IllegalAccessException {        

        boolean parameterWasSet = false;
        try {
            Field field = actor.getClass().getField(name);
            field.setAccessible(true);
            field.set(actor, value);
            parameterWasSet = true;
        } catch (NoSuchFieldException e) {
            parameterWasSet = false;
        }
        
        return parameterWasSet;
    }
}
