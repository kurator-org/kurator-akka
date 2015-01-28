package org.kurator.akka;

import org.restflow.yaml.spring.YamlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

public class YamlFileWorkflowRunner extends WorkflowRunner {

    public YamlFileWorkflowRunner(String definitionFilePath) throws Exception {
        super();
        GenericApplicationContext context = new GenericApplicationContext();
        YamlBeanDefinitionReader reader = new YamlBeanDefinitionReader(context);
        reader.registerBeanDefinitions(definitionFilePath);
        super.loadWorkflowFromSpringContext(context);
    }
}
