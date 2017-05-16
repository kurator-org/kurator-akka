package org.kurator.akka;

import org.yesworkflow.actors.python.PythonActorBuilder;

public class PythonScriptActor extends YesWorkflowActor {
    
    protected PythonScriptActor() {
        super(new PythonActorBuilder());
    }
}
