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
                .step(onStart)
                .initialize(onInitialize)                
                .outputStream(outStream)
                .errorStream(errStream)
                .build();

        actor.configure();
        actor.initialize();
    }
    
    @Override
    protected synchronized void onStart() throws Exception {
        
		actor.step();
			
        endStreamAndStop();
    }

}
