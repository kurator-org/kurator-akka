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
    public boolean broadcastNulls = false;

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

        if (onData != null) {
            ywActorBuilder.step(onData)
                          .input("inp")
                          .output("out");
        }
                
        ywActor = ywActorBuilder.build();

        ywActor.initialize();
    }
    
    @Override
    protected synchronized void onStart() throws Exception {
		ywActor.start();
        if (onData == null) endStreamAndStop();
    }

    @Override
    public synchronized void onData(Object value) throws Exception {
        ywActor.setInputValue("inp", value);
        ywActor.step();
        Object output = ywActor.getOutputValue("out");
        if (output != null || broadcastNulls) broadcast(output);
    }
    
    @Override
    protected synchronized void onEnd() throws Exception {
        ywActor.wrapup();
    }   
}
