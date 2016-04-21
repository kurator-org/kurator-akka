package org.kurator.akka;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Set;

import org.kurator.log.Log;
import org.kurator.log.Logger;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.IndirectActorProducer;

public class WorkflowProducer implements IndirectActorProducer {

    private ActorSystem system;
    private Set<ActorRef> actors;
    private String name;
    private ActorRef inputActor;
    private InputStream inStream;
    private PrintStream outStream;
    private PrintStream errStream;
    private WorkflowRunner workflowRunner;
    private Logger logger;

    public WorkflowProducer(ActorSystem system, Set<ActorRef> actors, String name, ActorRef inputActor, Logger logger,
            InputStream inStream, PrintStream outStream, PrintStream errStream, WorkflowRunner workflowRunner) {

       this.logger = logger;
       logger.setSource("WORKFLOW-PRODUCER");

       this.system = system;
       this.actors = actors;
       this.name = name;
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
        logger.trace("Producing Akka actor representing WORKFLOW");
        Workflow workflow = new Workflow(system, name, inStream, outStream, errStream, workflowRunner);
        workflow.setLogger(logger.createChild());
        workflow.setActors(actors);
        workflow.setInput(inputActor);
        return workflow;
    }
}
