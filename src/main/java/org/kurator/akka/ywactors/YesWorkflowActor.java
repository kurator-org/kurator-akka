package org.kurator.akka.ywactors;

import org.kurator.akka.KuratorActor;
import org.yesworkflow.actors.Actor;
import org.yesworkflow.actors.ScriptActorBuilder;

public class YesWorkflowActor extends KuratorActor {
    
    protected Actor ywActor;
    protected ScriptActorBuilder ywScriptActorBuilder;
    protected String onInitialize;
    protected String onStart;
    protected String onData;
    protected String onEnd;
    
    protected YesWorkflowActor(ScriptActorBuilder actorBuilder) {
        this.ywScriptActorBuilder = actorBuilder;
    }
    
    @Override
    protected synchronized void onInitialize() throws Exception {

        onInitialize = (String)configuration.get("onInit");
        onStart = (String)configuration.get("onStart");
        onData = (String)configuration.get("onData");
        onEnd = (String)configuration.get("onEnd");
        
        ywActor = ywScriptActorBuilder
                .initialize(onInitialize)       
                .start(onStart)
                .step(onData)
                .wrapup(onEnd)
                .outputStream(outStream)
                .errorStream(errStream)
                .build();

        ywActor.initialize();
    }
    
    @Override
    protected synchronized void onStart() throws Exception {
        
		ywActor.start();
			
        if (onData == null) {
            endStreamAndStop();       
        }
    }

    @Override
    public synchronized void onData(Object value) throws Exception {
    }
    
    @Override
    protected synchronized void onEnd() throws Exception {
        ywActor.wrapup();
    }   
}
