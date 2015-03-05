package org.kurator.akka;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Set;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.IndirectActorProducer;

public class WorkflowProducer implements IndirectActorProducer {

    private ActorSystem system;
    private Set<ActorRef> actors;
    private ActorRef inputActor;
    private InputStream inStream;
    private PrintStream outStream;
    private PrintStream errStream;
    private WorkflowRunner workflowRunner;
    
    public WorkflowProducer(ActorSystem system, Set<ActorRef> actors) {
        this.system = system;
        this.actors = actors;
    }

    public WorkflowProducer(ActorSystem system, Set<ActorRef> actors, ActorRef inputActor,
            InputStream inStream, PrintStream outStream, PrintStream errStream, WorkflowRunner workflowRunner) {
       this(system, actors);
       this.inputActor = inputActor;
       this.inStream = inStream;
       this.outStream = outStream;
       this.errStream = errStream;
       this.workflowRunner = workflowRunner;
    }
    
    @Override
    public Class<? extends Actor> actorClass() {
        return Workflow.class;
    }

    @Override
    public Workflow produce() {
        Workflow workflow = new Workflow(system, inStream, outStream, errStream, workflowRunner);        
        workflow.setActors(actors);
        workflow.setInput(inputActor);
        return workflow;
    }
}
