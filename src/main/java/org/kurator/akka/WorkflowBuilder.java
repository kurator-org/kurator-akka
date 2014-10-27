package org.kurator.akka;


import static akka.pattern.Patterns.ask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.messages.Initialize;
import org.springframework.context.support.GenericApplicationContext;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class WorkflowBuilder {

    private WorkflowConfiguration workflowConfiguration;
    private final ActorSystem system;
    private ActorRef inputActor = null;
    private Map<ActorBuilder,ActorRef> actorRefForActorConfig = new HashMap<ActorBuilder, ActorRef>();
    private ActorRef workflowRef;
    private ActorBuilder inputActorBuilder;
    List<ActorBuilder> actorConfigurations;

    public WorkflowBuilder() {
        this.system = ActorSystem.create("Workflow");
    }

    public ActorBuilder createActorBuilder() {
        if (actorConfigurations == null) {
            actorConfigurations = new LinkedList<ActorBuilder>();
        }
        ActorBuilder actor = new ActorBuilder();
        actorConfigurations.add(actor);
        return actor;
    }
    
    public WorkflowBuilder inputActor(ActorBuilder inputActorBuilder) {
        this.inputActorBuilder = inputActorBuilder;
        return this;
    }
    
    public ActorRef getWorkflowRef() {
        return workflowRef;
    }
    
    public ActorRef root() {
        return system.lookupRoot();
    }
    
    public ActorSystem getActorSystem() {
        return system;
    }
    
    public ActorRef getActorForConfig(ActorBuilder config) {
        return actorRefForActorConfig.get(config);
    }
    
    protected void loadWorkflowFromSpringContext(GenericApplicationContext context) throws Exception {

        context.refresh();

        // get the workflow configuration bean
        String workflowNames[] = context.getBeanNamesForType(Class.forName("org.kurator.akka.WorkflowConfiguration"));
        if (workflowNames.length != 1) {
            throw new Exception("Workflow definition must contain at exactly one instance of WorkflowConfiguration.");
        }
        workflowConfiguration = (WorkflowConfiguration) context.getBean(workflowNames[0]);
        
        inputActorBuilder = workflowConfiguration.getInputActor(); 

        actorConfigurations = workflowConfiguration.getActors();
    }

    public ActorRef build() {
        return build(actorConfigurations);
    }
    
    public ActorRef build(List<ActorBuilder> actorConfigurations) {
        
        Set<ActorRef> actors = new HashSet<ActorRef>();
        if (actorConfigurations.size() > 0) {
            for (ActorBuilder actorConfig : actorConfigurations) {
                ActorRef actor =  system.actorOf(Props.create(ActorProducer.class, actorConfig.actorClass(), actorConfig.getParameters(), actorConfig.getListeners(), this));
                actors.add(actor);
                actorRefForActorConfig.put(actorConfig, actor);
                if (inputActorBuilder == actorConfig) inputActor = actor;
            }
        }
    
        // create a workflow using the workflow configuration and comprising the actors
        workflowRef = system.actorOf(Props.create(WorkflowProducer.class, system, actors, inputActor));
        
        return workflowRef;
    }
    
    public void run() throws TimeoutException, InterruptedException {
        this.startWorkflow();
        this.awaitWorkflow();
    }
    
    public void tellWorkflow(Object message) {
        workflowRef.tell(message, system.lookupRoot());
    }
    
    public void startWorkflow() throws TimeoutException, InterruptedException {
        Future<Object> future = ask(workflowRef, new Initialize(), Constants.TIMEOUT);
        future.ready(Constants.TIMEOUT_DURATION, null);
    }
    
    public void awaitWorkflow() {
        system.awaitTermination();        
    }
}
