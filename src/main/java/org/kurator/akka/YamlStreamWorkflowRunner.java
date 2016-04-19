package org.kurator.akka;

import java.io.InputStream;

public class YamlStreamWorkflowRunner extends YamlWorkflowRunner {
    
    public YamlStreamWorkflowRunner() throws Exception {
        super();
    }
    
    public YamlStreamWorkflowRunner yamlStream(InputStream yamlStream) throws Exception {
        super.createActorSystem();
        logger.info("Reading YAML definition of workflow from input stream");
        yamlBeanReader.loadBeanDefinitions(yamlStream, "-");
        super.loadWorkflowFromSpringContext();
        return this;
    }
    
    public YamlStreamWorkflowRunner logger(Logger customLogger) {
        super.logger(customLogger);
        return this;
    }
}
