package org.kurator.akka;

import org.yesworkflow.actors.ActorBuilder;
import org.yesworkflow.actors.ScriptActor;

public class YesWorkflowActor extends KuratorActor {
    
    private final ActorBuilder actorBuilder;

    protected YesWorkflowActor(ActorBuilder actorBuilder) {
        this.actorBuilder = actorBuilder;
    }
    
    @Override
    protected synchronized void onStart() throws Exception {

    	String onStart = (String)configuration.get("onStart");
        
		ScriptActor actor = (ScriptActor)actorBuilder
             		   		.outputStream(outStream)
	             		    .errorStream(errStream)
	             		    .step(onStart)
	             		    .build();

		actor.configure();
		actor.initialize();
		actor.step();
			
        endStreamAndStop();
    }

}
