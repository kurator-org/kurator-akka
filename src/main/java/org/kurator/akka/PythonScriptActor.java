package org.kurator.akka;

import org.yesworkflow.actors.ScriptActor;
import org.yesworkflow.actors.python.PythonActorBuilder;

public class PythonScriptActor extends KuratorScriptActor {
    
    @Override
    protected synchronized void onStart() throws Exception {

    	String onStart = (String)configuration.get("onStart");
        
		ScriptActor actor = new PythonActorBuilder()
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
