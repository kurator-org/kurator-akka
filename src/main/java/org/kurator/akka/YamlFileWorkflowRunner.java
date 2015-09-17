package org.kurator.akka;

import org.kurator.exceptions.KuratorException;
import org.restflow.yaml.spring.YamlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

public class YamlFileWorkflowRunner extends WorkflowRunner {

    public YamlFileWorkflowRunner(String definitionFilePath) throws Exception {
        super();
        GenericApplicationContext context = new GenericApplicationContext();
        YamlBeanDefinitionReader reader = new YamlBeanDefinitionReader(context);
        
        try {
            reader.registerBeanDefinitions(definitionFilePath);
        } catch (Exception e) {
            String message = e.getMessage().replace("; ", ": " + EOL);
            throw new KuratorException(message);
        }
        
        super.loadWorkflowFromSpringContext(context);
    }
}
