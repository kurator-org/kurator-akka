package org.kurator.akka;

import org.kurator.exceptions.KuratorException;
import org.restflow.yaml.spring.YamlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

public abstract class YamlWorkflowRunner extends WorkflowRunner {
    
    protected final GenericApplicationContext springContext;
    protected final YamlBeanDefinitionReader yamlBeanReader;
    
    public YamlWorkflowRunner() throws Exception {
        super();
        springContext = new GenericApplicationContext();
        yamlBeanReader = new YamlBeanDefinitionReader(springContext);
    }
    
    protected void loadWorkflowFromSpringContext() throws Exception {
        
        try {
            springContext.refresh();
        } catch (Exception e) {
//            String message = e.getMessage().replace("; ", ": " + EOL);
            logger.critical(e.getMessage());
            throw new KuratorException(e.getMessage());
        }        
        
        // get the workflow configuration bean
        String workflowNames[] = springContext.getBeanNamesForType(Class.forName("org.kurator.akka.WorkflowConfig"));
        if (workflowNames.length == 0) {
            String message = "Workflow definition does not include a Workflow configuration object.";
            logger.critical(message);
            throw new KuratorException(message);
        } else  if (workflowNames.length > 1) {
            String message = "Workflow definition contains multiple Workflow configuration objects.";
            logger.critical(message);
            throw new KuratorException(message);
        }
        
        workflowName = workflowNames[0];
        logger.debug("Name of Workflow bean is: " + workflowName);
        
        WorkflowConfig workflowConfig = (WorkflowConfig) springContext.getBean(workflowName);
        
        super.inputActorConfig = workflowConfig.getInputActor(); 

        for (ActorConfig actor : workflowConfig.getActors()) {
            addActorConfig(actor);
        }

        super.workflowParameters = workflowConfig.getParameters();
    }
}
