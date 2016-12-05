package org.kurator.akka;

import org.yesworkflow.actors.ScriptActor;
import org.yesworkflow.actors.r.RActorBuilder;

public class KuratorScriptActor extends KuratorActor {
    
    @Override
    protected synchronized void onStart() throws Exception {

    	String onStart = (String)configuration.get("onStart");
        
		ScriptActor actor = new RActorBuilder()
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
