package org.kurator.akka.ywactors;

import org.yesworkflow.actors.r.RActorBuilder;

public class RScriptActor extends YesWorkflowActor {
    
    public RScriptActor() {
        super(new RActorBuilder());
    }
}
