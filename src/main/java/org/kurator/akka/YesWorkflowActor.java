package org.kurator.akka;

import org.yesworkflow.actors.Actor;
import org.yesworkflow.actors.ScriptActorBuilder;

public class YesWorkflowActor extends KuratorActor {
    
    private ScriptActorBuilder actorBuilder;
    private Actor actor;

    protected YesWorkflowActor(ScriptActorBuilder actorBuilder) {
        this.actorBuilder = actorBuilder;
    }
    
    @Override
    protected synchronized void onInitialize() throws Exception {

        String onStart = (String)configuration.get("onStart");
        String onInitialize = (String)configuration.get("onInit");
        
        actor = actorBuilder
                .initialize(onInitialize)       
                .start(onStart)
                .outputStream(outStream)
                .errorStream(errStream)
                .build();

        actor.initialize();
    }
    
    @Override
    protected synchronized void onStart() throws Exception {
        
		actor.start();
			
        endStreamAndStop();
    }

}
