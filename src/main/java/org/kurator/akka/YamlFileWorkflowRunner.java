package org.kurator.akka;

import org.kurator.exceptions.KuratorException;

public class YamlFileWorkflowRunner extends YamlWorkflowRunner {

    public YamlFileWorkflowRunner(String definitionFilePath) throws Exception {
        super();
        
        try {
            yamlBeanReader.registerBeanDefinitions(definitionFilePath);
        } catch (Exception e) {
            String message = e.getMessage().replace("; ", ": " + EOL);
            throw new KuratorException(message);
        }
        
        super.loadWorkflowFromSpringContext();
    }
}
