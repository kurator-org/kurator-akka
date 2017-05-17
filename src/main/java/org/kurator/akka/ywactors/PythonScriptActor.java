package org.kurator.akka.ywactors;

import org.yesworkflow.actors.python.PythonActorBuilder;

public class PythonScriptActor extends YesWorkflowActor {
    
    public PythonScriptActor() {
        super(new PythonActorBuilder());
    }
}
