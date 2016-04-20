package org.kurator.akka;

import org.kurator.exceptions.KuratorException;

public class YamlFileWorkflowRunner extends YamlWorkflowRunner {

    public YamlFileWorkflowRunner() throws Exception {
        super();
    }
    
    public YamlFileWorkflowRunner yamlFile(String definitionFilePath) throws Exception {
        super.createActorSystem();
        logger.info("Reading YAML definition of workflow from " + definitionFilePath);
        try {
            yamlBeanReader.registerBeanDefinitions(definitionFilePath);
        } catch (Exception e) {
            String message = e.getMessage().replace("; ", ": " + EOL);
            logger.fatal("Error reading YAML definition of workflow: " + message);
            throw new KuratorException(message);
        }
        
        super.loadWorkflowFromSpringContext();
        return this;
    }
    
    public YamlFileWorkflowRunner logger(Logger customLogger) {
        super.logger(customLogger);
        return this;
    }
}
