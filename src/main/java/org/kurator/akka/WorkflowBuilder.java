package org.kurator.akka;


import static akka.pattern.Patterns.ask;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.StartMessage;
import org.springframework.context.support.GenericApplicationContext;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

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
    private List<ActorBuilder> actorConfigurations;
    private Map<String, Object> workflowParameters;
    private PrintStream outStream = System.out;
    private PrintStream errStream = System.err;
    private Exception lastException = null;

    public WorkflowBuilder() {
        
        // create a configuration for the actor system that disables all logging from Akka
        Config config = ConfigFactory.load()
                .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
                .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
                .withValue("akka.actor.guardian-supervisor-strategy", 
                        ConfigValueFactory.fromAnyRef("akka.actor.DefaultSupervisorStrategy"))
                        ;
        
        // create the actor system itself
        this.system = ActorSystem.create("Workflow",  config);
    }

    public ActorBuilder createActorBuilder() {
        if (actorConfigurations == null) {
            actorConfigurations = new LinkedList<ActorBuilder>();
        }
        ActorBuilder actor = new ActorBuilder();
        actorConfigurations.add(actor);
        return actor;
    }
    
    public WorkflowBuilder outputStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }

    public WorkflowBuilder errorStream(PrintStream errStream) {
        this.errStream = errStream;
        return this;
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
        
        workflowParameters = workflowConfiguration.getParameters();
    }

    
    public WorkflowBuilder apply(Map<String, Object> workflowSettings) throws Exception {
        
        for (Map.Entry<String, Object> setting : workflowSettings.entrySet()) {
            String settingName = setting.getKey();
            Object settingValue = setting.getValue();
            apply(settingName, settingValue);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public WorkflowBuilder apply(String settingName, Object settingValue) throws Exception {
        
        Map<String,Object> workflowParameter = null;
        if (workflowParameters != null) {
            workflowParameter = (Map<String, Object>) workflowParameters.get(settingName);
        }
            
        if (workflowParameter == null) {
            throw new Exception("Workflow does not take parameter named " + settingName);
        }
                    
        ActorBuilder actor = (ActorBuilder) workflowParameter.get("actor");
        String actorParameterName = (String) workflowParameter.get("parameter");

        actor.parameter(actorParameterName, settingValue);
        
        return this;
    }

    
    public ActorRef build() {
        return build(actorConfigurations);
    }
    
    public ActorRef build(List<ActorBuilder> actorConfigurations) {
        
        Set<ActorRef> actors = new HashSet<ActorRef>();
        if (actorConfigurations.size() > 0) {
            int actorIndex = 0;
            for (ActorBuilder actorConfig : actorConfigurations) {
                String actorName = actorConfig.getName();
                if (actorName == null) {
                    actorName = actorConfig.actorClass().getName().toString() + "_" + actorIndex;
                }
                
                ActorRef actor =  system.actorOf(Props.create(
                                    ActorProducer.class, 
                                    actorConfig.actorClass(), 
                                    actorConfig.getDefaults(), 
                                    actorConfig.getParameters(), 
                                    actorConfig.getListeners(), 
                                    outStream,
                                    errStream,
                                    this
                                  ), actorName);
            
                actors.add(actor);
                actorRefForActorConfig.put(actorConfig, actor);
                if (inputActorBuilder == actorConfig) inputActor = actor;
                actorIndex++;
            }
        }
    
        // create a workflow using the workflow configuration and comprising the actors
        workflowRef = system.actorOf(Props.create(
                            WorkflowProducer.class, 
                            system, 
                            actors, 
                            inputActor, 
                            outStream,
                            errStream,
                            this
                       ));
        
        return workflowRef;
    }
    
    public void run() throws Exception {
        this.startWorkflow();
        this.awaitWorkflow();
    }
    
    public void tellWorkflow(Object message) {
        workflowRef.tell(message, system.lookupRoot());
    }
    
    public void startWorkflow() throws TimeoutException, InterruptedException {
        Future<Object> future = ask(workflowRef, new Initialize(), Constants.TIMEOUT);
        future.ready(Constants.TIMEOUT_DURATION, null);
        workflowRef.tell(new StartMessage(), system.lookupRoot());
    }
    
    public void setLastException(Exception e) {
        lastException = e;
    }
    
    public void awaitWorkflow() throws Exception {
        system.awaitTermination();
        if (lastException != null) {
            throw(lastException);
        }
    }
}
