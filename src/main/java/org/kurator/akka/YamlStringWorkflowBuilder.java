package org.kurator.akka;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.restflow.yaml.spring.YamlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

@SuppressWarnings("deprecation")
public class YamlStringWorkflowBuilder extends WorkflowBuilder {
    
    public YamlStringWorkflowBuilder(String definitionString) throws Exception {
        super();
        GenericApplicationContext context = new GenericApplicationContext();
        YamlBeanDefinitionReader reader = new YamlBeanDefinitionReader(context);
        InputStream stream = new StringBufferInputStream(definitionString);
        reader.loadBeanDefinitions(stream, "-");
        super.loadWorkflowFromSpringContext(context);
    }
}
