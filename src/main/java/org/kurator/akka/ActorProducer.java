package org.kurator.akka;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.actor.IndirectActorProducer;

public class ActorProducer implements IndirectActorProducer {

    private Class<? extends AkkaActor> actorClass;
    private Map<String, Object> defaults;
    private Map<String, Object> parameters;
    private List<ActorConfig> listeners;
    private String code = null;
    private String script = null;
    private String onStart = null;
    private String onEnd = null;
    private String onData = null;
    private String pythonClass = null;
    private WorkflowRunner workflowRunner;
    private AkkaActor actor;
    private InputStream inStream;
    private PrintStream outStream;
    private PrintStream errStream;

    public ActorProducer(
            Class<? extends AkkaActor> actorClass, 
            Map<String, Object> defaults, 
            Map<String, Object> parameters, 
            List<ActorConfig> listeners,
            String onStart,
            String script,
            String code,
            String onData,
            String pythonClass,
            InputStream inStream, 
            PrintStream outStream, 
            PrintStream errStream,
            WorkflowRunner workflowRunner) {
        
        this.actorClass = actorClass;
        this.defaults = defaults;
        this.parameters = parameters;
        this.listeners = listeners;
        this.workflowRunner = workflowRunner;
        this.onStart = onStart;
        this.script = script;
        this.code = code;
        this.onData = onData;
        this.pythonClass = pythonClass;
        this.inStream = inStream;
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
             .inputStream(inStream)
             .outputStream(outStream)
             .errorStream(errStream)
             .setCode(code)
             .setScript(script)
             .setOnStart(onStart)
             .setOnData(onData)
             .setPythonClass(pythonClass);

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
