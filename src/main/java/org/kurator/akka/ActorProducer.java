package org.kurator.akka;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.kurator.akka.actors.Transformer;

import akka.actor.IndirectActorProducer;

public class ActorProducer implements IndirectActorProducer {

    private Class<? extends Transformer> actorClass;
    private Map<String, Object> parameters;
    private List<ActorBuilder> listenerBuilders;
    private WorkflowBuilder workflowBuilder;
    private Transformer actor;
    private PrintStream outStream;
    private PrintStream errStream;

    public ActorProducer(Class<? extends Transformer> actorClass, Map<String, Object> parameters, List<ActorBuilder> listenerConfigs, 
            PrintStream outStream, PrintStream errStream, WorkflowBuilder workflowBuilder) {
        this.actorClass = actorClass;
        this.parameters = parameters;
        this.listenerBuilders = listenerConfigs;
        this.workflowBuilder = workflowBuilder;
        this.outStream = outStream;
        this.errStream = errStream;
    }

    @Override
    public Class<? extends Transformer> actorClass() {
        return actorClass;
    }

    @Override
    public Transformer produce() {
        
        try {
            actor = actorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        actor.setListenerConfigs(listenerBuilders);
        actor.setWorkflowRunner(workflowBuilder);
        actor.outputStream(outStream);
        actor.errorStream(errStream);
        
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
