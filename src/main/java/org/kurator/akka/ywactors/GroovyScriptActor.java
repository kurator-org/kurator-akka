package org.kurator.akka.ywactors;

import org.yesworkflow.actors.groovy.GroovyActorBuilder;

public class GroovyScriptActor extends YesWorkflowActor {
    
    public GroovyScriptActor() {
        super(new GroovyActorBuilder());
    }
}
