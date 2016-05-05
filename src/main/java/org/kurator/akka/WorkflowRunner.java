package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.kurator.akka.data.WorkflowProduct;
import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.Failure;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Start;
import org.kurator.exceptions.KuratorException;
import org.kurator.log.DefaultLogger;
import org.kurator.log.Log;
import org.kurator.log.Logger;
import org.kurator.log.SilentLogger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class WorkflowRunner {

    public static final String EOL = System.getProperty("line.separator");

    private ActorSystem system;
    private ActorRef inputActor = null;
    private Map<ActorConfig,ActorRef> actorRefForActorConfig = new HashMap<ActorConfig, ActorRef>();
    private Map<String,ActorConfig> actorConfigForActorName = new HashMap<String, ActorConfig>();
    private Map<ActorRef,String> actorNameForActorRef = new HashMap<ActorRef,String>();
    private ActorRef workflow;
    protected ActorConfig inputActorConfig;
    protected Map<String, Object> workflowParameters;
    private InputStream inStream = System.in;
    private PrintStream outStream = System.out;
    private PrintStream errStream = System.err;
    private Exception lastException = null;
    private int actorIndex = 0;
    protected String workflowName = "Workflow";
    protected Logger logger = new DefaultLogger();
    private Config actorSystemConfig;
    private List<WorkflowProduct> workflowProducts = new LinkedList<WorkflowProduct>();

    static {
        PythonActor.updateClasspath();
    }
        
    public WorkflowRunner() {
        
        logger = new SilentLogger();
        
        // create a configuration for the actor system that disables all logging from Akka
        actorSystemConfig = ConfigFactory.load()
                .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
                .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
                .withValue("akka.actor.guardian-supervisor-strategy", 
                        ConfigValueFactory.fromAnyRef("akka.actor.DefaultSupervisorStrategy"));
    }
    
    protected WorkflowRunner createActorSystem() throws Exception {
        logger.debug("Instantiating ActorSystem");
        logger.value("Applying ActorSystem configuration", actorSystemConfig.root().toString());
        this.system = ActorSystem.create("Workflow",  this.actorSystemConfig);
        return this;
    }

    public WorkflowRunner logger(Logger customLogger) {
        logger = customLogger;
        logger.setSource("RUNNER");
        return this;
    }
    
    public ActorConfig actor(ActorConfig actorConfig, Class<? extends KuratorActor> actorClass) {
        actorConfig.actorClass(actorClass);
        addActorConfig(actorConfig);
        return actorConfig;
    }
    
    public ActorConfig actor(Class<? extends KuratorActor> actorClass) {
        return actor(new ActorConfig(), actorClass);
    }
    
    protected ActorConfig addActorConfig(ActorConfig actorConfig) {
        String actorName = actorConfig.getName();
        if (actorName == null) {
            actorName = actorConfig.actorClass().getSimpleName().toString() + "_" + ++actorIndex;
            actorConfig.name(actorName);
        }
        logger.debug("Added configuration for actor " + actorName);
        actorConfigForActorName.put(actorName, actorConfig);
        return actorConfig;
    }

    public WorkflowRunner name(String name) {
        this.workflowName = name;
        logger.debug("Setting workflow name to " + name);
        return this;
    }
    
    public String name() {
        return this.workflowName;
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
        logger.debug("Setting workflow input actor to " + inputActorConfig.getName());
        return this;
    }
    
    public ActorRef getWorkflowRef() {
        return workflow;
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

        logger.value("Setting workflow parameter", workflowName + "." + settingName, settingValue);
        
        Map<String,Object> workflowParameter = null;
        if (workflowParameters != null) {
            workflowParameter = (Map<String, Object>) workflowParameters.get(settingName);
        }
            
        if (workflowParameter != null) {
            ActorConfig actor = (ActorConfig) workflowParameter.get("actor");
            String actorParameterName = (String) workflowParameter.get("parameter");
            logger.value("Setting actor parameter", actor.getName() + "." + actorParameterName,  settingValue);
            actor.param(actorParameterName, settingValue);
            return this;
        }
        
        StringTokenizer nameComponents = new StringTokenizer(settingName, ".");
        if (nameComponents.countTokens() > 1) {
            String actorName = nameComponents.nextToken();
            String parameterName = nameComponents.nextToken();
            logger.value("Setting actor parameter ", actorName + "." + parameterName,  settingValue);
            ActorConfig actor = actorConfigForActorName.get(actorName);
            
            if (actor == null) {
                String message = "Workflow contains no actor with name " + actorName;
                logger.error(message);
                throw new Exception(message);
            }
            
            actor.param(parameterName, settingValue);
            return this;
        }
        
        logger.error("Workflow does not have a parameter named " + settingName);
        throw new Exception("Workflow does not have a parameter named " + settingName);
    }
    
    public WorkflowRunner build() throws Exception {

        if (this.system == null) this.createActorSystem();
        
        Collection<ActorConfig> actorConfigs = actorConfigForActorName.values();
        if (actorConfigs.isEmpty()) {
            logger.error("Workflow definition contains no actors");
            throw new KuratorException("Workflow definition contains no actors.");
        }
        
        Set<ActorRef> actors = new HashSet<ActorRef>();
        if (actorConfigs.size() > 0) {
            for (ActorConfig actorConfig : actorConfigs) {
                String actorName = actorConfig.getName();
                logger.info("Instantiating " + Log.ACTOR(actorName));
                ActorRef actor =  this.system.actorOf(Props.create(
                                    ActorProducer.class, 
                                    actorConfig.actorClass(), 
                                    actorConfig.getConfig(),
                                    actorConfig.getDefaults(), 
                                    actorConfig.getParameters(),
                                    actorConfig.getListeners(),
                                    actorConfig.getInputs(),
                                    actorConfig.getMetadataReaders(),
                                    actorConfig.getMetadataWriters(),
                                    logger.createChild(),
                                    inStream,
                                    outStream,
                                    errStream,
                                    this
                                  ), actorName);
            
                actors.add(actor);
                actorRefForActorConfig.put(actorConfig, actor);
                actorNameForActorRef.put(actor, actorConfig.getName());
                if (inputActorConfig == actorConfig) {
                    logger.info("Setting input actor for workflow to :" + Log.ACTOR(actorConfig.getName()));
                    inputActor = actor;
                }
            }
        }
    
        // create a workflow using the workflow configuration and comprising the actors
        logger.info("Instantiating WORKFLOW");
        workflow = system.actorOf(Props.create(
                            WorkflowProducer.class, 
                            system, 
                            actors, 
                            workflowName,
                            inputActor,
                            logger.createChild(),
                            inStream,
                            outStream,
                            errStream,
                            this
                       ));

        return this;
    }
    
    
    public WorkflowRunner tellWorkflow(Object message) {
        logger.comm("Sending message to WORKFLOW: " + message);
        workflow.tell(message, system.lookupRoot());
        return this;
    }

    public WorkflowRunner tellWorkflow(Object... messages) {
        for (Object message : messages) {
            tellWorkflow(message);
        }
        return this;
    }

    public WorkflowRunner tellActor(ActorConfig actor, Object message) {
        ActorRef actorRef = this.actorRefForActorConfig.get(actor);
        logger.comm("Sending message to actor " + actor.getName() + ": " + message);
        actorRef.tell(message, system.lookupRoot());
        return this;
    }

    public WorkflowRunner tellActor(ActorConfig actor, Object... messages) {
        for (Object message : messages) {
            tellActor(actor, message);
        }
        return this;
    }
    
    public synchronized String name(ActorRef actor) {
        return  actorNameForActorRef.get(actor);
    }
    
    public WorkflowRunner init() throws Exception {
        logger.debug("Initializing WORKFLOW");
        logger.comm("Sending INITIALIZE message to WORKFLOW");
        Future<Object> future = ask(workflow, new Initialize(), Constants.TIMEOUT);
        logger.comm("Waiting for INITIALIZE response from WORKFLOW");
        future.ready(Constants.TIMEOUT_DURATION, null);
        ControlMessage result = (ControlMessage)future.value().get().get();
        if (result instanceof Failure) {
            throw new KuratorException(result.toString());
        }
        logger.comm("Received INITIALIZE response from WORKFLOW");
        return this;
    }
    
    public WorkflowRunner start() throws Exception {
        logger.info("Workflow run starting");        
        logger.comm("Sending START message to WORKFLOW");
        workflow.tell(new Start(), system.lookupRoot());
        return this;
    }
    
    public WorkflowRunner begin() throws Exception {
        this.build()
            .init()
            .start();
        return this;
    }

    @SuppressWarnings("deprecation")
    public WorkflowRunner end() throws Exception {
        logger.debug("Waiting for WORKFLOW to run to completion");
        logger.trace("Waiting for ActorSystem to terminate");
        system.awaitTermination();
        logger.trace("ActorSystem terminated");
        logger.info("Workflow run completed");
        if (lastException != null) {
            logger.error("Exception thrown during workflow run: " + lastException);
            throw(lastException);
        }
        
        return this;
    }
       
    public WorkflowRunner run() throws Exception {
        this.begin()
            .end();
        return this;
    }

    public WorkflowRunner runAsync(Runnable callback) throws Exception {
        system.registerOnTermination(callback);
        this.begin();
        return this;
    }

    public void setLastException(Exception e) {
        lastException = e;
    }
    
    public synchronized void setWorkflowProducts(List<WorkflowProduct> products) {
        
        logger.comm("Received workflow PRODUCTS from WORKFLOW");
        this.workflowProducts = products;

        logger.info("Workflow run yielded " + workflowProducts.size() + " PRODUCTS.");
        for (int i = 0; i < workflowProducts.size(); ++i) {
            WorkflowProduct product = workflowProducts.get(i);
            logger.value("Workflow product (" + (i+1) + "): " + product.toString());
        }
    }
    
    public List<WorkflowProduct> getWorkflowProducts() {
        return workflowProducts;
    }
}
