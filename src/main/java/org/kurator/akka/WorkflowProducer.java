package org.kurator.akka;

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
    private PrintStream outStream;
    private PrintStream errStream;
    
    public WorkflowProducer(ActorSystem system, Set<ActorRef> actors) {
        this.system = system;
        this.actors = actors;
    }

    public WorkflowProducer(ActorSystem system, Set<ActorRef> actors, ActorRef inputActor,
            PrintStream outStream, PrintStream errStream) {
       this(system, actors);
       this.inputActor = inputActor;
       this.outStream = outStream;
       this.errStream = errStream;
    }
    
    @Override
    public Class<? extends Actor> actorClass() {
        return Workflow.class;
    }

    @Override
    public Workflow produce() {
        Workflow workflow = new Workflow(system, outStream, errStream);        
        workflow.setActors(actors);
        workflow.setInput(inputActor);
        return workflow;
    }
}
