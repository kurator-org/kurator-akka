package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.Failure;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Start;
import org.kurator.exceptions.KuratorException;
import org.springframework.context.support.GenericApplicationContext;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class WorkflowRunner {

    public static final String EOL = System.getProperty("line.separator");

    private WorkflowConfig workflowConfig;
    private final ActorSystem system;
    private ActorRef inputActor = null;
    private Map<ActorConfig,ActorRef> actorRefForActorConfig = new HashMap<ActorConfig, ActorRef>();
    private Map<String,ActorConfig> actorConfigForActorName = new HashMap<String, ActorConfig>();
    private ActorRef workflow;
    private ActorConfig inputActorConfig;
    private Map<String, Object> workflowParameters;
    private InputStream inStream = System.in;
    private PrintStream outStream = System.out;
    private PrintStream errStream = System.err;
    private Exception lastException = null;
    private int actorIndex = 0;
    private String name = "Workflow";
    
    static {
        String localPythonPackages = System.getenv("KURATOR_LOCAL_PACKAGES");
        if (localPythonPackages != null) {
            try {
            addToClasspath(localPythonPackages);
            } catch (Throwable t) {
                t.printStackTrace();
            }            
        }
    }
    
    static void addToClasspath(String path) throws Exception {
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
        method.setAccessible(true);
        method.invoke(classLoader, new Object[] { new URL("file:" + path + "/") });
    }
    
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

    public ActorConfig actor(ActorConfig actorConfig, Class<? extends AkkaActor> actorClass) {
        actorConfig.actorClass(actorClass);
        addActorConfig(actorConfig);
        return actorConfig;
    }

    
    public ActorConfig actor(Class<? extends AkkaActor> actorClass) {
        return actor(new ActorConfig(), actorClass);
    }
    
    private ActorConfig addActorConfig(ActorConfig actorConfig) {
        String actorName = actorConfig.getName();
        if (actorName == null) {
            actorName = actorConfig.actorClass().getName().toString() + "_" + ++actorIndex;
            actorConfig.name(actorName);
        }
        actorConfigForActorName.put(actorName, actorConfig);
        return actorConfig;
    }

    public WorkflowRunner name(String name) {
        this.name = name;
        return this;
    }
    
    public String name() {
        return this.name;
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
    
    protected void loadWorkflowFromSpringContext(GenericApplicationContext context) throws Exception {

        context.refresh();

        // get the workflow configuration bean
        String workflowNames[] = context.getBeanNamesForType(Class.forName("org.kurator.akka.WorkflowConfig"));
        if (workflowNames.length != 1) {
            throw new Exception("Workflow definition must contain at exactly one instance of WorkflowConfig.");
        }
        
        name = workflowNames[0];
        
        workflowConfig = (WorkflowConfig) context.getBean(name);
        
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
            actor.param(actorParameterName, settingValue);
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
            
            actor.param(parameterName, settingValue);
            return this;
        }
        
        throw new Exception("Workflow does not take parameter named " + settingName);
    }
    
    public WorkflowRunner build() {
        
        Collection<ActorConfig> actorConfigs = actorConfigForActorName.values();
        Set<ActorRef> actors = new HashSet<ActorRef>();
        if (actorConfigs.size() > 0) {
            for (ActorConfig actorConfig : actorConfigs) {
                String actorName = actorConfig.getName();                
                ActorRef actor =  system.actorOf(Props.create(
                                    ActorProducer.class, 
                                    actorConfig.actorClass(), 
                                    actorConfig.getConfig(),
                                    actorConfig.getDefaults(), 
                                    actorConfig.getParameters(), 
                                    actorConfig.getListeners(),
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
        workflow = system.actorOf(Props.create(
                            WorkflowProducer.class, 
                            system, 
                            actors, 
                            name,
                            inputActor,
                            inStream,
                            outStream,
                            errStream,
                            this
                       ));

        return this;
    }
    
    
    public WorkflowRunner tellWorkflow(Object message) {
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
        actorRef.tell(message, system.lookupRoot());
        return this;
    }

    public WorkflowRunner tellActor(ActorConfig actor, Object... messages) {
        for (Object message : messages) {
            tellActor(actor, message);
        }
        return this;
    }
    
    public WorkflowRunner init() throws Exception {
        Future<Object> future = ask(workflow, new Initialize(), Constants.TIMEOUT);
        future.ready(Constants.TIMEOUT_DURATION, null);
        ControlMessage result = (ControlMessage)future.value().get().get();
        if (result instanceof Failure) {
            throw new KuratorException(result.toString());
        }
        return this;
    }
    
    public WorkflowRunner start() throws Exception {        
        workflow.tell(new Start(), system.lookupRoot());
        return this;
    }
    
    public WorkflowRunner begin() throws Exception {
        this.build()
            .init()
            .start();
        return this;
    }
    
    public WorkflowRunner end() throws Exception {
        system.awaitTermination();
        if (lastException != null) {
            throw(lastException);
        }
        return this;
    }
    
    public WorkflowRunner run() throws Exception {
        this.begin()
            .end();
        return this;
    }

    public void setLastException(Exception e) {
        lastException = e;
    }
}
