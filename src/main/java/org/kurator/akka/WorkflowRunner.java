package org.kurator.akka;


import static akka.pattern.Patterns.ask;

import java.util.HashMap;
import java.util.HashSet;
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

public class WorkflowRunner {

    private WorkflowConfiguration workflowConfiguration;
    private final ActorSystem system;
    private ActorRef inputActor = null;
    private Map<ActorConfiguration,ActorRef> actorRefForActorConfig = new HashMap<ActorConfiguration, ActorRef>();
    private ActorRef workflowRef;

    public WorkflowRunner() {
        this.system = ActorSystem.create("Workflow");
    }

    public WorkflowRunner(List<ActorConfiguration> actorConfigurations, ActorConfiguration inputActorConfig) {
        this.system = ActorSystem.create("Workflow");
        instantiateWorkflow(actorConfigurations, inputActorConfig);
    }
    
    public ActorRef getWorkflowRef() {
        return workflowRef;
    }
    
    public ActorSystem getActorSystem() {
        return system;
    }
    
    public ActorRef getActorForConfig(ActorConfiguration config) {
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
        
        ActorConfiguration inputActorConfiguration = workflowConfiguration.getInputActor(); 

       instantiateWorkflow(workflowConfiguration.getActors(), inputActorConfiguration);
    }

  
    public ActorRef instantiateWorkflow(List<ActorConfiguration> actorConfigurations, ActorConfiguration inputActorConfig) {
        
        Set<ActorRef> actors = new HashSet<ActorRef>();
        if (actorConfigurations.size() > 0) {
            for (ActorConfiguration actorConfig : actorConfigurations) {
                ActorRef actor =  system.actorOf(Props.create(ActorBuilder.class, actorConfig.getActorClass(), actorConfig.getParameters(), actorConfig.getListeners(), this));
                actors.add(actor);
                actorRefForActorConfig.put(actorConfig, actor);
                if (inputActorConfig == actorConfig) inputActor = actor;
            }
        }
    
        // create a workflow using the workflow configuration and comprising the actors
        workflowRef = system.actorOf(Props.create(WorkflowBuilder.class, system, actors, inputActor));
        
        return workflowRef;
    }
    
    public void run() throws TimeoutException, InterruptedException {
        this.start();
        this.await();
    }
    
    public void start() throws TimeoutException, InterruptedException {
        Future<Object> future = ask(workflowRef, new Initialize(), Constants.TIMEOUT);
        future.ready(Constants.TIMEOUT_DURATION, null);
    }
    
    public void await() {
        system.awaitTermination();        
    }
}
