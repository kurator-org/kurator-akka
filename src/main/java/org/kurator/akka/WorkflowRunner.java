package org.kurator.akka;


import static akka.pattern.Patterns.ask;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Start;
import org.springframework.context.support.GenericApplicationContext;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class WorkflowRunner {

    private WorkflowConfig workflowConfig;
    private final ActorSystem system;
    private ActorRef inputActor = null;
    private Map<ActorConfig,ActorRef> actorRefForActorConfig = new HashMap<ActorConfig, ActorRef>();
    private Map<String,ActorConfig> actorConfigForActorName = new HashMap<String, ActorConfig>();
    private ActorRef workflowRef;
    private ActorConfig inputActorConfig;
    private Map<String, Object> workflowParameters;
    private InputStream inStream = System.in;
    private PrintStream outStream = System.out;
    private PrintStream errStream = System.err;
    private Exception lastException = null;
    private int actorIndex = 0;

    public WorkflowRunner() {
        
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

    public ActorConfig actor(Class<? extends AkkaActor> actorClass) {
        ActorConfig actor = new ActorConfig().actorClass(actorClass);
        addActorConfig(actor);
        return actor;
    }
    
    private ActorConfig addActorConfig(ActorConfig actorConfig) {
        String actorName = actorConfig.actorName;
        if (actorName == null) {
            actorName = actorConfig.actorClass().getName().toString() + "_" + actorIndex;
            actorConfig.actorName = actorName;
        }
        actorConfigForActorName.put(actorName, actorConfig);
        actorIndex++;
        return actorConfig;
    }

    public WorkflowRunner inputStream(InputStream inStream) {
        this.inStream = inStream;
        return this;
    }
    
    public WorkflowRunner outputStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }

    public WorkflowRunner errorStream(PrintStream errStream) {
        this.errStream = errStream;
        return this;
    }

    public WorkflowRunner inputActor(ActorConfig inputActorConfig) {
        this.inputActorConfig = inputActorConfig;
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
    
    public ActorRef getActorForConfig(ActorConfig config) {
        return actorRefForActorConfig.get(config);
    }
    
    protected void loadWorkflowFromSpringContext(GenericApplicationContext context) throws Exception {

        context.refresh();

        // get the workflow configuration bean
        String workflowNames[] = context.getBeanNamesForType(Class.forName("org.kurator.akka.WorkflowConfig"));
        if (workflowNames.length != 1) {
            throw new Exception("Workflow definition must contain at exactly one instance of WorkflowConfig.");
        }
        workflowConfig = (WorkflowConfig) context.getBean(workflowNames[0]);
        
        inputActorConfig = workflowConfig.getInputActor(); 

        for (ActorConfig actor : workflowConfig.getActors()) {
            addActorConfig(actor);
        }

        workflowParameters = workflowConfig.getParameters();
    }

    
    public WorkflowRunner apply(Map<String, Object> workflowSettings) throws Exception {
        
        for (Map.Entry<String, Object> setting : workflowSettings.entrySet()) {
            String settingName = setting.getKey();
            Object settingValue = setting.getValue();
            apply(settingName, settingValue);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public WorkflowRunner apply(String settingName, Object settingValue) throws Exception {
        
        Map<String,Object> workflowParameter = null;
        if (workflowParameters != null) {
            workflowParameter = (Map<String, Object>) workflowParameters.get(settingName);
        }
            
        if (workflowParameter != null) {
            ActorConfig actor = (ActorConfig) workflowParameter.get("actor");
            String actorParameterName = (String) workflowParameter.get("parameter");
            actor.parameter(actorParameterName, settingValue);
            return this;
        }
        
        StringTokenizer nameComponents = new StringTokenizer(settingName, ".");
        if (nameComponents.countTokens() > 1) {
            String actorName = nameComponents.nextToken();
            String parameterName = nameComponents.nextToken();
            ActorConfig actor = actorConfigForActorName.get(actorName);
            
            if (actor == null) {
                throw new Exception("Workflow contains no actor with name " + actorName);
            }
            
            actor.parameter(parameterName, settingValue);
            return this;
        }
        
        throw new Exception("Workflow does not take parameter named " + settingName);
    }
    
    public ActorRef build() {
        
        Collection<ActorConfig> actorConfigs = actorConfigForActorName.values();
        Set<ActorRef> actors = new HashSet<ActorRef>();
        if (actorConfigs.size() > 0) {
            for (ActorConfig actorConfig : actorConfigs) {
                String actorName = actorConfig.getName();                
                ActorRef actor =  system.actorOf(Props.create(
                                    ActorProducer.class, 
                                    actorConfig.actorClass(), 
                                    actorConfig.getDefaults(), 
                                    actorConfig.getParameters(), 
                                    actorConfig.getListeners(),
                                    actorConfig.getTrigger(),
                                    actorConfig.getScript(),
                                    actorConfig.getCode(),
                                    actorConfig.getOnData(),
                                    inStream,
                                    outStream,
                                    errStream,
                                    this
                                  ), actorName);
            
                actors.add(actor);
                actorRefForActorConfig.put(actorConfig, actor);
                if (inputActorConfig == actorConfig) inputActor = actor;
            }
        }
    
        // create a workflow using the workflow configuration and comprising the actors
        workflowRef = system.actorOf(Props.create(
                            WorkflowProducer.class, 
                            system, 
                            actors, 
                            inputActor, 
                            inStream,
                            outStream,
                            errStream,
                            this
                       ));
        
        return workflowRef;
    }
    
    
    public void tellWorkflow(Object message) {
        workflowRef.tell(message, system.lookupRoot());
    }
    
    public void start() throws TimeoutException, InterruptedException {
        Future<Object> future = ask(workflowRef, new Initialize(), Constants.TIMEOUT);
        future.ready(Constants.TIMEOUT_DURATION, null);
        workflowRef.tell(new Start(), system.lookupRoot());
    }
    
    public void await() throws Exception {
        system.awaitTermination();
        if (lastException != null) {
            throw(lastException);
        }
    }

    public void run() throws Exception {
        this.start();
        this.await();
    }

    public void setLastException(Exception e) {
        lastException = e;
    }
    
}
