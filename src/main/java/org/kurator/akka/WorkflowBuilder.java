package org.kurator.akka;

import java.util.Set;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.IndirectActorProducer;

public class WorkflowBuilder implements IndirectActorProducer {

    private ActorSystem system;
    private Set<ActorRef> actors;
    private ActorRef inputActor;
    
    public WorkflowBuilder(ActorSystem system, Set<ActorRef> actors) {
        this.system = system;
        this.actors = actors;
    }

    public WorkflowBuilder(ActorSystem system, Set<ActorRef> actors, ActorRef inputActor) {
       this(system, actors);
       this.inputActor = inputActor;
    }
    
    @Override
    public Class<? extends Actor> actorClass() {
        return Workflow.class;
    }

    @Override
    public Workflow produce() {
        Workflow workflow = new Workflow(system);
        workflow.setActors(actors);
        workflow.setInput(inputActor);
        return workflow;
    }
}
