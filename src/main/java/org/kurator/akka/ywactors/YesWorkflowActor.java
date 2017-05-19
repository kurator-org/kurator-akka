package org.kurator.akka.ywactors;

import org.kurator.akka.KuratorActor;
import org.yesworkflow.actors.Actor;
import org.yesworkflow.actors.ScriptActorBuilder;

public class YesWorkflowActor extends KuratorActor {
    
    protected Actor ywActor;
    protected ScriptActorBuilder ywActorBuilder;
    protected String onInitialize;
    protected String onStart;
    protected String onData;
    protected String onEnd;
    protected boolean broadcastNulls = false;
    protected String inputName;
    protected String outputName;
    
    protected YesWorkflowActor(ScriptActorBuilder actorBuilder) {
        this.ywActorBuilder = actorBuilder;
    }
    
    @Override
    protected synchronized void onInitialize() throws Exception {

        onInitialize = (String)configuration.get("onInit");
        onStart = (String)configuration.get("onStart");
        onData = (String)configuration.get("onData");
        onEnd = (String)configuration.get("onEnd");
        
        ywActorBuilder.initialize(onInitialize)       
                      .start(onStart)
                      .wrapup(onEnd)
                      .outputStream(outStream)
                      .errorStream(errStream);

        inputName = (inputs.size() == 1) ? inputs.values().iterator().next() : "input";
        outputName = (String)configuration.get("outputName");
        if (outputName == null) outputName = "output";
        
        
        if (onData != null || onStart != null) {
            ywActorBuilder.step(onData)
                          .input(inputName)
                          .output(outputName);
        }
                
        ywActor = ywActorBuilder.build();

        ywActor.initialize();
    }
    
    @Override
    protected synchronized void onStart() throws Exception {
		ywActor.start();
        Object output = ywActor.getOutputValue(outputName);
        if (output != null || broadcastNulls) broadcast(output);
        if (onData == null) endStreamAndStop();
    }

    @Override
    public synchronized void onData(Object value) throws Exception {
        ywActor.setInputValue(inputName, value);
        ywActor.step();
        Object output = ywActor.getOutputValue(outputName);
        if (output != null || broadcastNulls) broadcast(output);
    }
    
    @Override
    protected synchronized void onEnd() throws Exception {
        ywActor.wrapup();
    }   
}
