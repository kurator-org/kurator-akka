package org.kurator.akka;

import org.yesworkflow.actors.r.RActorBuilder;

public class RScriptActor extends YesWorkflowActor {
    
    protected RScriptActor() {
        super(new RActorBuilder());
    }
}
