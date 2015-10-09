package org.kurator.akka;

import org.kurator.exceptions.KuratorException;
import org.restflow.yaml.spring.YamlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

public class YamlWorkflowRunner extends WorkflowRunner {
    
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
            throw new KuratorException(e.getMessage());
        }        
        
        // get the workflow configuration bean
        String workflowNames[] = springContext.getBeanNamesForType(Class.forName("org.kurator.akka.WorkflowConfig"));
        if (workflowNames.length == 0) {
            throw new KuratorException("Workflow definition does not include a Workflow configuration object.");
        } else  if (workflowNames.length > 1) {
            throw new KuratorException("Workflow definition contains multiple Workflow configuration objects.");
        }
        
        workflowName = workflowNames[0];
        
        WorkflowConfig workflowConfig = (WorkflowConfig) springContext.getBean(workflowName);
        
        super.inputActorConfig = workflowConfig.getInputActor(); 

        for (ActorConfig actor : workflowConfig.getActors()) {
            addActorConfig(actor);
        }

        super.workflowParameters = workflowConfig.getParameters();
    }
}
